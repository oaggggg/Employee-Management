package com.emp.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emp.admin.common.BusinessException;
import com.emp.admin.common.DataScopeHelper;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.ResultCode;
import com.emp.admin.common.Roles;
import com.emp.admin.context.UserContext;
import com.emp.admin.dto.CreateUserDTO;
import com.emp.admin.dto.EmployeeQueryDTO;
import com.emp.admin.entity.Department;
import com.emp.admin.entity.Employee;
import com.emp.admin.mapper.DepartmentMapper;
import com.emp.admin.mapper.EmployeeMapper;
import com.emp.admin.service.EmployeeService;
import com.emp.admin.service.SysUserService;
import com.emp.admin.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 员工业务实现
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final DepartmentMapper departmentMapper;

    /** 新增员工时开通账号、员工离职时禁用账号，都要用到账号服务 */
    private final SysUserService sysUserService;

    /** 数据权限：部门负责人只能看本部门的员工 */
    private final DataScopeHelper dataScopeHelper;

    @Override
    public PageResult<EmployeeVO> pageQuery(EmployeeQueryDTO query) {
        // 数据权限：部门负责人只能看自己部门里的员工（前端就算传了别的部门也会被覆盖掉）
        if (UserContext.isManager()) {
            query.setDeptId(dataScopeHelper.requireDeptId());
        }
        Page<EmployeeVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 自定义 SQL 写在 EmployeeMapper.xml 里，分页插件会自动处理 LIMIT 和 count
        IPage<EmployeeVO> result = baseMapper.selectEmployeePage(page, query);
        return PageResult.of(result);
    }

    @Override
    public EmployeeVO getDetail(Long id) {
        Employee employee = getById(id);
        if (employee == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
        // 数据权限：部门负责人只能查看本部门的员工详情
        dataScopeHelper.checkEmployeeInMyDept(id);
        EmployeeVO vo = new EmployeeVO();
        // 把实体里的属性复制到 VO（属性名相同即可）
        BeanUtils.copyProperties(employee, vo);
        Department department = departmentMapper.selectById(employee.getDeptId());
        vo.setDeptName(department == null ? "" : department.getName());
        // 详情接口也带上"是否已开通登录账号"，前端编辑弹窗要据此显示
        vo.setAccountUsername(sysUserService.getAccountUsernameByEmployeeId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEmployee(Employee employee) {
        // 数据权限：部门负责人只能在本部门下新增员工
        // 部门直接强制成他管理的那个部门，前端就算传了别的部门也不生效
        if (UserContext.isManager()) {
            employee.setDeptId(dataScopeHelper.requireDeptId());
        }
        checkEmpNoUnique(employee.getEmpNo(), null);
        checkDeptExists(employee.getDeptId());
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        save(employee);

        // 员工入职时可以直接开通登录账号（用户名默认用工号），这是"入职 → 开通账号"闭环的起点。
        // 员工和账号是两次写操作，用事务保证要么都成功要么都回滚。
        if (Boolean.TRUE.equals(employee.getOpenAccount())) {
            CreateUserDTO dto = new CreateUserDTO();
            dto.setUsername(employee.getEmpNo());
            dto.setRealName(employee.getName());
            dto.setRole(accountRole(employee));
            dto.setEmployeeId(employee.getId());
            sysUserService.createUser(dto);
        }
    }

    /**
     * 开通账号时用什么角色
     * <p>
     * 管理员可以指定（不填默认普通员工）；
     * 部门负责人只能开普通员工的账号，不能给自己或别人开管理员。
     */
    private String accountRole(Employee employee) {
        if (UserContext.isManager()) {
            return Roles.EMPLOYEE;
        }
        return StringUtils.hasText(employee.getAccountRole()) ? employee.getAccountRole() : Roles.EMPLOYEE;
    }

    @Override
    public void updateEmployee(Employee employee) {
        Employee old = employee.getId() == null ? null : getById(employee.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
        // 数据权限：部门负责人只能改本部门的员工，并且不能把员工调到别的部门去
        if (UserContext.isManager()) {
            dataScopeHelper.checkEmployeeInMyDept(employee.getId());
            employee.setDeptId(dataScopeHelper.requireDeptId());
        }
        checkEmpNoUnique(employee.getEmpNo(), employee.getId());
        checkDeptExists(employee.getDeptId());
        updateById(employee);

        // 员工状态和登录账号联动：离职自动禁用账号（并被踢下线），复职自动恢复
        if (employee.getStatus() != null && !Objects.equals(old.getStatus(), employee.getStatus())) {
            if (employee.getStatus() == 0) {
                sysUserService.disableByEmployeeId(employee.getId());
            } else {
                sysUserService.enableByEmployeeId(employee.getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeEmployee(Long id) {
        Employee employee = getById(id);
        if (employee == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
        // 数据权限：部门负责人只能删本部门的员工；另外谁也不能删掉自己的员工档案
        if (UserContext.isManager()) {
            dataScopeHelper.checkEmployeeInMyDept(id);
        }
        if (Objects.equals(id, dataScopeHelper.currentEmployeeIdOrNull())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能删除自己");
        }
        // 逻辑删除只是把 deleted 改成 1，这一行数据还在表里。
        // 而工号上建了唯一索引，如果不腾出来，以后再用同一个工号新增员工就会报唯一索引冲突。
        // 所以先把工号改名（E1001 -> E1001_del_1），再做逻辑删除。
        // 这是两条写操作，所以加上 @Transactional：第一步成功、第二步失败时会一起回滚。
        Employee release = new Employee();
        release.setId(id);
        release.setEmpNo(employee.getEmpNo() + "_del_" + id);
        updateById(release);

        // 人走了账号也要停：禁用并踢出登录账号（账号数据保留，方便后续审计）
        sysUserService.disableByEmployeeId(id);

        removeById(id);
    }

    /** 工号不能重复；修改时排除自己 */
    private void checkEmpNoUnique(String empNo, Long excludeId) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>()
                .eq(Employee::getEmpNo, empNo);
        if (excludeId != null) {
            wrapper.ne(Employee::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS, "工号已存在");
        }
    }

    /** 部门必须存在，否则会存出一条查不到部门的脏数据 */
    private void checkDeptExists(Long deptId) {
        if (deptId == null || departmentMapper.selectById(deptId) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "所选部门不存在");
        }
    }
}
