package com.emp.admin.controller;

import com.emp.admin.annotation.LogOperation;
import com.emp.admin.annotation.RequireAdmin;
import com.emp.admin.annotation.RequireManager;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.Result;
import com.emp.admin.dto.DepartmentQueryDTO;
import com.emp.admin.entity.Department;
import com.emp.admin.service.DepartmentService;
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

import java.util.List;

/**
 * 部门管理接口
 * <p>
 * 分页查询、增删改：只有管理员（部门管理是管理员专属模块）
 * 部门下拉列表：管理员看全部，部门负责人只看自己所在的部门（员工表单要用）
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /** 分页查询：GET /api/departments/page?pageNum=1&pageSize=10&name=研发 */
    @GetMapping("/page")
    @RequireAdmin
    public Result<PageResult<Department>> page(DepartmentQueryDTO query) {
        return Result.success(departmentService.pageQuery(query));
    }

    /** 查询所有部门：给员工表单的部门下拉框用 */
    @GetMapping("/list")
    @RequireManager
    public Result<List<Department>> list() {
        return Result.success(departmentService.listAll());
    }

    /** 新增部门 */
    @PostMapping
    @RequireAdmin
    @LogOperation(module = "部门管理", operation = "新增部门")
    public Result<Void> save(@RequestBody @Valid Department department) {
        departmentService.saveDepartment(department);
        return Result.success();
    }

    /** 修改部门：id 从路径取，避免前端传参不一致 */
    @PutMapping("/{id}")
    @RequireAdmin
    @LogOperation(module = "部门管理", operation = "修改部门")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid Department department) {
        department.setId(id);
        departmentService.updateDepartment(department);
        return Result.success();
    }

    /** 删除部门（逻辑删除，且部门下有员工时不允许删） */
    @DeleteMapping("/{id}")
    @RequireAdmin
    @LogOperation(module = "部门管理", operation = "删除部门")
    public Result<Void> remove(@PathVariable Long id) {
        departmentService.removeDepartment(id);
        return Result.success();
    }
}
