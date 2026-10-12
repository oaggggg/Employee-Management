package com.emp.admin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 操作日志查询条件
 */
@Data
public class LogQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    /** 操作人用户名，模糊查询 */
    private String username;

    /** 所属模块 */
    private String module;

    /** 账号 id 集合：部门负责人只能看本部门成员的日志（由后端填充） */
    private List<Long> userIds;

    /** 开始日期（含当天） */
    private LocalDate startDate;

    /** 结束日期（含当天） */
    private LocalDate endDate;
}
