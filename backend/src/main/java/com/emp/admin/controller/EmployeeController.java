package com.emp.admin.controller;

import com.emp.admin.annotation.LogOperation;
import com.emp.admin.annotation.RequireManager;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.Result;
import com.emp.admin.dto.EmployeeQueryDTO;
import com.emp.admin.entity.Employee;
import com.emp.admin.service.EmployeeService;
import com.emp.admin.vo.EmployeeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工管理接口
 * <p>
 * 管理员：全公司都能管
 * 部门负责人：只能管自己部门里的员工（新增时部门会被强制设成他管理的部门，
 * 修改和删除只能针对本部门员工，跨部门一律 403），数据范围在 Service 层控制
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /** 分页 + 条件查询：GET /api/employees/page?pageNum=1&pageSize=10&name=张&deptId=1&status=1 */
    @GetMapping("/page")
    @RequireManager
    public Result<PageResult<EmployeeVO>> page(EmployeeQueryDTO query) {
        return Result.success(employeeService.pageQuery(query));
    }

    /** 查询员工详情（编辑时回显） */
    @GetMapping("/{id}")
    @RequireManager
    public Result<EmployeeVO> detail(@PathVariable Long id) {
        return Result.success(employeeService.getDetail(id));
    }

    /** 新增员工（可同时开通登录账号） */
    @PostMapping
    @RequireManager
    @LogOperation(module = "员工管理", operation = "新增员工")
    public Result<Void> save(@RequestBody @Valid Employee employee) {
        employeeService.saveEmployee(employee);
        return Result.success();
    }

    /** 修改员工 */
    @PutMapping("/{id}")
    @RequireManager
    @LogOperation(module = "员工管理", operation = "修改员工")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid Employee employee) {
        employee.setId(id);
        employeeService.updateEmployee(employee);
        return Result.success();
    }

    /** 删除员工（逻辑删除） */
    @DeleteMapping("/{id}")
    @RequireManager
    @LogOperation(module = "员工管理", operation = "删除员工")
    public Result<Void> remove(@PathVariable Long id) {
        employeeService.removeEmployee(id);
        return Result.success();
    }
}
