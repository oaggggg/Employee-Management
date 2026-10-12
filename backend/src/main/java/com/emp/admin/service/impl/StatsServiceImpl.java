package com.emp.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emp.admin.common.DataScopeHelper;
import com.emp.admin.common.LeaveStatus;
import com.emp.admin.context.UserContext;
import com.emp.admin.entity.Employee;
import com.emp.admin.entity.Leave;
import com.emp.admin.mapper.DepartmentMapper;
import com.emp.admin.mapper.EmployeeMapper;
import com.emp.admin.mapper.LeaveMapper;
import com.emp.admin.service.StatsService;
import com.emp.admin.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 首页统计业务实现
 * <p>
 * 三种角色的首页都能调这个接口，但看到的范围不同：
 * - 管理员：全公司
 * - 部门负责人：自己管理的那一个部门
 * - 普通员工：只统计自己（前端只展示欢迎语，不展示统计卡片）
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final LeaveMapper leaveMapper;
    private final DataScopeHelper dataScopeHelper;

    @Override
    public StatsVO overview() {
        Long deptId = null;
        Long employeeId = null;
        if (UserContext.isManager()) {
            // 部门负责人：限定在自己管理的部门
            deptId = dataScopeHelper.requireDeptId();
        } else if (!UserContext.isAdmin()) {
            // 普通员工：只统计自己
            employeeId = dataScopeHelper.currentEmployeeIdOrNull();
        }

        StatsVO vo = new StatsVO();
        vo.setEmployeeCount(countEmployee(deptId, employeeId, null));
        vo.setOnJobCount(countEmployee(deptId, employeeId, 1));
        vo.setDepartmentCount(countDepartment());
        vo.setMonthHireCount(countMonthHire(deptId, employeeId));
        vo.setPendingLeaveCount(countPendingLeave(deptId, employeeId));
        return vo;
    }

    /** 员工数量，可以按部门、按某个员工、按在职状态过滤 */
    private Long countEmployee(Long deptId, Long employeeId, Integer status) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>()
                .eq(deptId != null, Employee::getDeptId, deptId)
                .eq(employeeId != null, Employee::getId, employeeId)
                .eq(status != null, Employee::getStatus, status);
        return employeeMapper.selectCount(wrapper);
    }

    /** 管理员看全公司有多少个部门；其它角色只关心自己所在的那一个部门 */
    private Long countDepartment() {
        if (UserContext.isAdmin()) {
            return departmentMapper.selectCount(null);
        }
        return dataScopeHelper.currentDeptIdOrNull() == null ? 0L : 1L;
    }

    /** 本月入职人数（入职日期在当月的记录数） */
    private Long countMonthHire(Long deptId, Long employeeId) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>()
                .eq(deptId != null, Employee::getDeptId, deptId)
                .eq(employeeId != null, Employee::getId, employeeId)
                .apply("date_format(hire_date, '%Y-%m') = date_format(now(), '%Y-%m')");
        return employeeMapper.selectCount(wrapper);
    }

    /** 待审批的请假单数量 */
    private Long countPendingLeave(Long deptId, Long employeeId) {
        LambdaQueryWrapper<Leave> wrapper = new LambdaQueryWrapper<Leave>()
                .eq(Leave::getStatus, LeaveStatus.PENDING.getCode());
        if (employeeId != null) {
            wrapper.eq(Leave::getEmployeeId, employeeId);
        } else if (deptId != null) {
            wrapper.in(Leave::getEmployeeId,
                    dataScopeHelper.orEmptyPlaceholder(dataScopeHelper.employeeIdsOfDept(deptId)));
        }
        return leaveMapper.selectCount(wrapper);
    }
}
