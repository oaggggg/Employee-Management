package com.emp.admin.controller;

import com.emp.admin.annotation.RequireManager;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.Result;
import com.emp.admin.dto.LogQueryDTO;
import com.emp.admin.service.OperationLogService;
import com.emp.admin.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志接口
 * <p>
 * 管理员能看到所有人的操作痕迹；部门负责人只能看到本部门成员的操作
 * （数据范围在 Service 层控制）
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService operationLogService;

    /** 日志分页查询：GET /api/logs/page?pageNum=1&pageSize=10&username=&module=&startDate=&endDate= */
    @GetMapping("/page")
    @RequireManager
    public Result<PageResult<OperationLogVO>> page(LogQueryDTO query) {
        return Result.success(operationLogService.pageQuery(query));
    }
}
