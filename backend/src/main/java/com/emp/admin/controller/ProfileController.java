package com.emp.admin.controller;

import com.emp.admin.annotation.LogOperation;
import com.emp.admin.common.Result;
import com.emp.admin.dto.ChangePasswordDTO;
import com.emp.admin.service.SysUserService;
import com.emp.admin.vo.ProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心接口
 * <p>
 * 管理员和普通员工都能访问，所以没有加 @RequireAdmin，只要登录就能用。
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final SysUserService sysUserService;

    /** 查看自己的账号信息和关联的员工资料 */
    @GetMapping
    public Result<ProfileVO> profile() {
        return Result.success(sysUserService.getProfile());
    }

    /**
     * 修改自己的密码
     * 修改成功后当前登录态会立即失效（需要重新登录）
     */
    @PutMapping("/password")
    @LogOperation(module = "个人中心", operation = "修改密码")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        sysUserService.changePassword(dto);
        return Result.success();
    }
}
