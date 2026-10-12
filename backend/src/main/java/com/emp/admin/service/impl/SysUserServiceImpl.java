package com.emp.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emp.admin.common.BusinessException;
import com.emp.admin.common.DataScopeHelper;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.RedisKeys;
import com.emp.admin.common.ResultCode;
import com.emp.admin.common.Roles;
import com.emp.admin.context.UserContext;
import com.emp.admin.dto.ChangePasswordDTO;
import com.emp.admin.dto.CreateUserDTO;
import com.emp.admin.dto.LoginDTO;
import com.emp.admin.dto.UserQueryDTO;
import com.emp.admin.entity.Department;
import com.emp.admin.entity.Employee;
import com.emp.admin.entity.SysUser;
import com.emp.admin.mapper.DepartmentMapper;
import com.emp.admin.mapper.EmployeeMapper;
import com.emp.admin.mapper.SysUserMapper;
import com.emp.admin.service.SysUserService;
import com.emp.admin.util.JwtUtil;
import com.emp.admin.vo.LoginVO;
import com.emp.admin.vo.ProfileVO;
import com.emp.admin.vo.UserInfoVO;
import com.emp.admin.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用户/登录/账号业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    /** 新账号的默认初始密码 */
    public static final String DEFAULT_PASSWORD = "123456";

    /** BCrypt 加密工具：同一个密码每次加密结果都不同（自带随机盐），比对用 matches */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    /** 数据权限：部门负责人只能看/改本部门员工的账号 */
    private final DataScopeHelper dataScopeHelper;

    @Value("${emp.login.max-fail-count}")
    private int maxFailCount;

    @Value("${emp.login.lock-minutes}")
    private long lockMinutes;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String failKey = RedisKeys.LOGIN_FAIL + username;

        // 1. 先看这个账号是不是因为连续输错密码被锁了（防止暴力破解）
        String failCount = redisTemplate.opsForValue().get(failKey);
        if (failCount != null && Integer.parseInt(failCount) >= maxFailCount) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 2. 根据用户名查用户
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            recordLoginFail(failKey);
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }

        // 3. 校验密码：数据库存的是 BCrypt 密文，只能用 matches 比对，不能解密
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFail(failKey);
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }

        // 4. 校验账号状态（员工离职、管理员手动禁用都会置为 0）
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 5. 生成 JWT（把角色也放进去，拦截器做权限校验时就不用再查库了），
        //    并把 token 存进 Redis，key 用 userId，实现"一个账号只保留一个有效登录态"
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        redisTemplate.opsForValue().set(RedisKeys.LOGIN_TOKEN + user.getId(), token,
                jwtUtil.getExpireMinutes(), TimeUnit.MINUTES);

        // 6. 登录成功，清掉失败次数
        redisTemplate.delete(failKey);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(toUserInfo(user));
        return loginVO;
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        SysUser user = getById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return toUserInfo(user);
    }

    @Override
    public void logout() {
        kickOut(UserContext.getUserId());
    }

    // ==================== 账号管理 ====================

    @Override
    public PageResult<UserVO> pageQuery(UserQueryDTO query) {
        // 数据权限：部门负责人只能看到本部门员工的账号
        if (UserContext.isManager()) {
            query.setEmployeeIds(dataScopeHelper.orEmptyPlaceholder(
                    dataScopeHelper.employeeIdsOfDept(dataScopeHelper.requireDeptId())));
        }
        Page<UserVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectUserPage(page, query));
    }

    @Override
    public Long createUser(CreateUserDTO dto) {
        // 数据权限：部门负责人只能给本部门员工开通账号，而且角色强制为普通员工
        // （不限制的话，负责人可以给自己开一个管理员账号，那就越权了）
        if (UserContext.isManager()) {
            if (dto.getEmployeeId() == null) {
                throw new BusinessException(ResultCode.FORBIDDEN, "只能给本部门员工开通账号");
            }
            dataScopeHelper.checkEmployeeInMyDept(dto.getEmployeeId());
            dto.setRole(Roles.EMPLOYEE);
        }
        if (count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS, "用户名已存在");
        }
        // 一个员工只能有一个登录账号（数据库里也建了唯一索引兜底）
        if (dto.getEmployeeId() != null) {
            Employee employee = employeeMapper.selectById(dto.getEmployeeId());
            if (employee == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "所选员工不存在");
            }
            if (existsByEmployeeId(dto.getEmployeeId())) {
                throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
            }
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setRealName(StringUtils.hasText(dto.getRealName()) ? dto.getRealName() : dto.getUsername());
        user.setRole(dto.getRole());
        user.setEmployeeId(dto.getEmployeeId());
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : DEFAULT_PASSWORD));
        save(user);
        return user.getId();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账号不存在");
        }
        // 数据权限：部门负责人只能操作本部门员工的账号
        dataScopeHelper.checkUserInMyDept(id);
        if (status != null && status == 0 && Objects.equals(id, UserContext.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能禁用自己正在使用的账号");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
        if (status != null && status == 0) {
            // 禁用之后，如果不清掉 Redis 里的 token，这个人还能继续用到 token 过期
            kickOut(id);
        }
    }

    @Override
    public void resetPassword(Long id, String password) {
        if (getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账号不存在");
        }
        // 数据权限：部门负责人只能重置本部门员工账号的密码
        dataScopeHelper.checkUserInMyDept(id);
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(
                StringUtils.hasText(password) ? password : DEFAULT_PASSWORD));
        updateById(update);
        kickOut(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账号不存在");
        }
        if (Objects.equals(id, UserContext.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能删除自己正在使用的账号");
        }
        // 数据权限：部门负责人只能删除本部门员工的账号
        dataScopeHelper.checkUserInMyDept(id);
        // 用户名和关联的员工 id 上都建了唯一索引，逻辑删除后数据行还在表里，
        // 所以要把这两个唯一值腾出来（用户名改名、员工关联置空），否则以后不能再建同名账号、
        // 该员工也无法重新开通账号。
        // 注意：MyBatis-Plus 默认不更新 null 字段，要把 employeeId 置空必须用 UpdateWrapper 显式 set。
        update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getUsername, user.getUsername() + "_del_" + id)
                .set(SysUser::getEmployeeId, null));
        removeById(id);
        kickOut(id);
    }

    // ==================== 个人中心 ====================

    @Override
    public ProfileVO getProfile() {
        SysUser user = getById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        ProfileVO vo = new ProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setEmployeeId(user.getEmployeeId());

        // 账号绑定了员工时，把员工资料一起返回；管理员账号可能没有绑定员工
        if (user.getEmployeeId() != null) {
            Employee employee = employeeMapper.selectById(user.getEmployeeId());
            if (employee != null) {
                vo.setEmpNo(employee.getEmpNo());
                vo.setPosition(employee.getPosition());
                vo.setPhone(employee.getPhone());
                vo.setEmail(employee.getEmail());
                vo.setHireDate(employee.getHireDate());
                vo.setEmployeeStatus(employee.getStatus());
                Department department = departmentMapper.selectById(employee.getDeptId());
                vo.setDeptName(department == null ? "" : department.getName());
            }
        }
        return vo;
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "新密码不能和原密码相同");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(update);
        // 密码变了，让当前登录态失效，需要重新登录（其它端也会被踢下线）
        kickOut(userId);
    }

    // ==================== 账号与员工状态联动 ====================

    @Override
    public void disableByEmployeeId(Long employeeId) {
        SysUser user = getByEmployeeId(employeeId);
        if (user == null) {
            return;
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setStatus(0);
        updateById(update);
        kickOut(user.getId());
        log.info("员工 {} 离职/被删除，已禁用其登录账号 {}", employeeId, user.getUsername());
    }

    @Override
    public void enableByEmployeeId(Long employeeId) {
        SysUser user = getByEmployeeId(employeeId);
        if (user == null) {
            return;
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setStatus(1);
        updateById(update);
        log.info("员工 {} 复职，已恢复其登录账号 {}", employeeId, user.getUsername());
    }

    @Override
    public boolean existsByEmployeeId(Long employeeId) {
        if (employeeId == null) {
            return false;
        }
        return count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmployeeId, employeeId)) > 0;
    }

    @Override
    public String getAccountUsernameByEmployeeId(Long employeeId) {
        SysUser user = getByEmployeeId(employeeId);
        return user == null ? null : user.getUsername();
    }

    @Override
    public void kickOut(Long userId) {
        if (userId != null) {
            redisTemplate.delete(RedisKeys.LOGIN_TOKEN + userId);
        }
    }

    /** 按员工 id 查账号（一个员工最多一个账号，所以可以放心用 getOne） */
    private SysUser getByEmployeeId(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmployeeId, employeeId));
    }

    /** 记录一次登录失败；第一次失败时设置过期时间，避免这个 key 一直占着内存 */
    private void recordLoginFail(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(failKey, lockMinutes, TimeUnit.MINUTES);
        }
    }

    private UserInfoVO toUserInfo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
