package com.emp.admin.controller;

import com.emp.admin.annotation.LogOperation;
import com.emp.admin.annotation.RequireManager;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.Result;
import com.emp.admin.dto.LeaveApplyDTO;
import com.emp.admin.dto.LeaveApproveDTO;
import com.emp.admin.dto.LeaveQueryDTO;
import com.emp.admin.service.LeaveService;
import com.emp.admin.vo.LeaveVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 请假接口
 * <p>
 * 提交申请、查看我的请假、撤销：登录即可
 * 查看全部请假单、审批：管理员和部门负责人（@RequireManager）
 * —— 其中部门负责人只能查看和审批本部门员工的请假单，部门范围在 Service 层控制
 */
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /** 提交请假申请 */
    @PostMapping
    @LogOperation(module = "请假管理", operation = "提交请假申请")
    public Result<Void> apply(@RequestBody @Valid LeaveApplyDTO dto) {
        leaveService.apply(dto);
        return Result.success();
    }

    /** 我的请假列表：GET /api/leaves/my?pageNum=1&pageSize=10&status= */
    @GetMapping("/my")
    public Result<PageResult<LeaveVO>> my(LeaveQueryDTO query) {
        return Result.success(leaveService.pageMy(query));
    }

    /** 撤销自己的申请（只能撤待审批的） */
    @PutMapping("/{id}/cancel")
    @LogOperation(module = "请假管理", operation = "撤销请假申请")
    public Result<Void> cancel(@PathVariable Long id) {
        leaveService.cancel(id);
        return Result.success();
    }

    /** 全部请假单（管理员 / 部门负责人）：GET /api/leaves/page?pageNum=1&pageSize=10&status=&employeeName= */
    @GetMapping("/page")
    @RequireManager
    public Result<PageResult<LeaveVO>> page(LeaveQueryDTO query) {
        return Result.success(leaveService.pageQuery(query));
    }

    /** 审批：通过（1）或驳回（2） */
    @PutMapping("/{id}/approve")
    @RequireManager
    @LogOperation(module = "请假管理", operation = "审批请假")
    public Result<Void> approve(@PathVariable Long id, @RequestBody @Valid LeaveApproveDTO dto) {
        leaveService.approve(id, dto);
        return Result.success();
    }
}
