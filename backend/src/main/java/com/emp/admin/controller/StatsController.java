package com.emp.admin.controller;

import com.emp.admin.common.Result;
import com.emp.admin.service.StatsService;
import com.emp.admin.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计接口
 * <p>
 * 三种角色都能访问，但返回的数据范围不同：管理员是全公司，
 * 部门负责人是自己管理的部门，普通员工只统计自己。
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** 首页统计数字（按当前登录用户的角色返回不同范围） */
    @GetMapping("/overview")
    public Result<StatsVO> overview() {
        return Result.success(statsService.overview());
    }
}
