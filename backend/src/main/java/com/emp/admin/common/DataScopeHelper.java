package com.emp.admin.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emp.admin.context.UserContext;
import com.emp.admin.entity.Employee;
import com.emp.admin.entity.SysUser;
import com.emp.admin.mapper.EmployeeMapper;
import com.emp.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据权限助手
 * <p>
 * 功能权限（能不能调这个接口）用注解 + 拦截器控制；
 * 数据权限（同一个接口能看/能改哪些数据）统一收敛到这里：
 * - 管理员：不限制，能看全公司
 * - 部门负责人：只能看/改本部门的数据（部门范围来自"账号 → 员工 → 部门"）
 * <p>
 * 部门、账号、操作日志、请假这几个模块都复用它，避免每个 Service 里各写一套。
 */
@Component
@RequiredArgsConstructor
public class DataScopeHelper {

    private final SysUserMapper sysUserMapper;
    private final EmployeeMapper employeeMapper;

    /** 当前登录账号绑定的员工 id，没绑定返回 null */
    public Long currentEmployeeIdOrNull() {
        SysUser user = sysUserMapper.selectById(UserContext.getUserId());
        return user == null ? null : user.getEmployeeId();
    }

    /** 当前登录账号所属的部门 id，取不到返回 null */
    public Long currentDeptIdOrNull() {
        Long employeeId = currentEmployeeIdOrNull();
        if (employeeId == null) {
            return null;
        }
        Employee employee = employeeMapper.selectById(employeeId);
        return employee == null ? null : employee.getDeptId();
    }

    /** 当前登录账号所属的部门 id，取不到直接抛 403（负责人必须绑定到某个部门的员工） */
    public Long requireDeptId() {
        Long deptId = currentDeptIdOrNull();
        if (deptId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前账号未关联部门，无法查看或操作部门数据");
        }
        return deptId;
    }

    /** 某个部门下的所有员工 id */
    public List<Long> employeeIdsOfDept(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        return employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getDeptId, deptId))
                .stream().map(Employee::getId).collect(Collectors.toList());
    }

    /** 某个部门下所有员工的登录账号 id（操作日志按部门过滤时用） */
    public List<Long> userIdsOfDept(Long deptId) {
        List<Long> employeeIds = employeeIdsOfDept(deptId);
        if (employeeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getEmployeeId, employeeIds))
                .stream().map(SysUser::getId).collect(Collectors.toList());
    }

    /**
     * 查询用的 id 集合：为空时返回一个不存在的 id，
     * 这样 SQL 里的 in 条件查不出任何数据（而不是因为没有条件变成查全部）
     */
    public List<Long> orEmptyPlaceholder(List<Long> ids) {
        return ids.isEmpty() ? Collections.singletonList(-1L) : ids;
    }

    /** 校验某个员工是不是当前负责人管理的部门里的员工 */
    public void checkEmployeeInMyDept(Long employeeId) {
        if (UserContext.isAdmin()) {
            return;
        }
        Employee employee = employeeId == null ? null : employeeMapper.selectById(employeeId);
        if (employee == null || !Objects.equals(employee.getDeptId(), requireDeptId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能操作本部门的员工");
        }
    }

    /** 校验某个登录账号是不是当前负责人管理的部门里的账号 */
    public void checkUserInMyDept(Long userId) {
        if (UserContext.isAdmin()) {
            return;
        }
        SysUser user = userId == null ? null : sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账号不存在");
        }
        // 没绑定员工的账号（比如管理员）employeeId 为 null，这里会直接判为不在本部门
        checkEmployeeInMyDept(user.getEmployeeId());
    }
}
