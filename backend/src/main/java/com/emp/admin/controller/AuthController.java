package com.emp.admin.controller;

import com.emp.admin.common.Result;
import com.emp.admin.dto.LoginDTO;
import com.emp.admin.service.SysUserService;
import com.emp.admin.vo.LoginVO;
import com.emp.admin.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口：登录、退出、获取当前用户
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    /**
     * 登录（唯一一个不需要 token 的接口，在 WebMvcConfig 里被放行）
     * 前端：POST /api/auth/login  { "username": "admin", "password": "123456" }
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        return Result.success(sysUserService.login(loginDTO));
    }

    /** 退出登录：删除 Redis 里的登录态 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        sysUserService.logout();
        return Result.success();
    }

    /** 获取当前登录用户信息（前端刷新页面后用它回显用户名） */
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return Result.success(sysUserService.getCurrentUserInfo());
    }
}
