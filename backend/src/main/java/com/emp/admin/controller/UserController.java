package com.emp.admin.controller;

import com.emp.admin.annotation.LogOperation;
import com.emp.admin.annotation.RequireManager;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.Result;
import com.emp.admin.dto.CreateUserDTO;
import com.emp.admin.dto.ResetPasswordDTO;
import com.emp.admin.dto.UpdateStatusDTO;
import com.emp.admin.dto.UserQueryDTO;
import com.emp.admin.service.SysUserService;
import com.emp.admin.vo.UserVO;
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
 * 账号管理接口
 * <p>
 * 查询、启用禁用、重置密码：管理员和部门负责人都能访问
 * （部门负责人只会看到并能操作本部门员工的账号，数据范围在 Service 层控制）
 * 开通账号、删除账号：只有管理员
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    /** 账号分页查询：GET /api/users/page?pageNum=1&pageSize=10&username=&role= */
    @GetMapping("/page")
    @RequireManager
    public Result<PageResult<UserVO>> page(UserQueryDTO query) {
        return Result.success(sysUserService.pageQuery(query));
    }

    /** 开通账号（给某个员工开通，或管理员建一个不绑定员工的账号；负责人只能给本部门员工开通） */
    @PostMapping
    @RequireManager
    @LogOperation(module = "账号管理", operation = "开通账号")
    public Result<Long> save(@RequestBody @Valid CreateUserDTO dto) {
        return Result.success(sysUserService.createUser(dto));
    }

    /** 启用 / 禁用账号 */
    @PutMapping("/{id}/status")
    @RequireManager
    @LogOperation(module = "账号管理", operation = "启用/禁用账号")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateStatusDTO dto) {
        sysUserService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    /** 重置密码（不传密码时默认重置为 123456） */
    @PutMapping("/{id}/password")
    @RequireManager
    @LogOperation(module = "账号管理", operation = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) @Valid ResetPasswordDTO dto) {
        sysUserService.resetPassword(id, dto == null ? null : dto.getPassword());
        return Result.success();
    }

    /** 删除账号（逻辑删除；负责人只能删本部门员工的账号） */
    @DeleteMapping("/{id}")
    @RequireManager
    @LogOperation(module = "账号管理", operation = "删除账号")
    public Result<Void> remove(@PathVariable Long id) {
        sysUserService.removeUser(id);
        return Result.success();
    }
}
