package com.emp.admin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emp.admin.common.Roles;
import com.emp.admin.entity.Department;
import com.emp.admin.entity.Employee;
import com.emp.admin.entity.SysUser;
import com.emp.admin.mapper.DepartmentMapper;
import com.emp.admin.mapper.EmployeeMapper;
import com.emp.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 首次启动初始化账号
 * <p>
 * 为什么不在 SQL 脚本里直接 insert？
 * 因为数据库里存的是 BCrypt 密文，密文每次加密结果都不一样（自带随机盐），
 * 手写在 SQL 里既不好维护也容易写错，交给程序在启动时生成更稳妥。
 * <p>
 * CommandLineRunner 会在 Spring 容器启动完成后执行一次，方法内部做了幂等判断，
 * 重复启动不会重复插入。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        initAdmin();
        initManagerAccounts();
        initDemoEmployeeAccount();
    }

    /** 初始化管理员账号：不绑定员工，角色是 ADMIN */
    private void initAdmin() {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin"));
        if (count != null && count > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("系统管理员");
        admin.setRole(Roles.ADMIN);
        admin.setStatus(1);
        sysUserMapper.insert(admin);
        log.info("首次启动，已初始化管理员账号：admin / 123456（角色：管理员）");
    }

    /**
     * 给各个部门的负责人开通登录账号（角色：部门负责人 MANAGER）
     * <p>
     * 判定方式：部门表里的 leader 字段是负责人姓名，去员工表里找"同部门 + 同名"的那条员工记录，
     * 用他的工号作为用户名开通账号。这样部门负责人登录后就能审批本部门员工的请假。
     */
    private void initManagerAccounts() {
        List<Department> departments = departmentMapper.selectList(null);
        for (Department department : departments) {
            if (!StringUtils.hasText(department.getLeader())) {
                continue;
            }
            Employee leader = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                    .eq(Employee::getName, department.getLeader())
                    .eq(Employee::getDeptId, department.getId())
                    .last("limit 1"));
            if (leader == null) {
                continue;
            }
            // 已经开过账号（或工号已被占用）就跳过，保证重复启动幂等
            if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmployeeId, leader.getId())) > 0) {
                continue;
            }
            if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, leader.getEmpNo())) > 0) {
                continue;
            }
            SysUser user = new SysUser();
            user.setUsername(leader.getEmpNo());
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName(leader.getName());
            user.setRole(Roles.MANAGER);
            user.setEmployeeId(leader.getId());
            user.setStatus(1);
            sysUserMapper.insert(user);
            log.info("首次启动，已为部门负责人 {}（{}）开通账号：{} / 123456（角色：部门负责人）",
                    department.getName(), leader.getName(), leader.getEmpNo());
        }
    }

    /**
     * 初始化一个演示用的员工账号，绑定示例数据里的第一个员工（工号 E1001）
     * <p>
     * 作用：不用先手动开通账号，就能直接体验"普通员工登录 → 提交请假 → 管理员审批"这条链路
     */
    private void initDemoEmployeeAccount() {
        Employee employee = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getEmpNo, "E1001")
                .last("limit 1"));
        if (employee == null) {
            return;
        }
        Long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeId, employee.getId()));
        if (exists != null && exists > 0) {
            return;
        }
        SysUser user = new SysUser();
        // 员工账号默认用工号当用户名，和页面里"开通账号"的默认值保持一致
        user.setUsername(employee.getEmpNo());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(employee.getName());
        user.setRole(Roles.EMPLOYEE);
        user.setEmployeeId(employee.getId());
        user.setStatus(1);
        sysUserMapper.insert(user);
        log.info("首次启动，已初始化演示员工账号：{} / 123456（绑定员工：{}）",
                employee.getEmpNo(), employee.getName());
    }
}
