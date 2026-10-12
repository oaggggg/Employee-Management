package com.emp.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志返回对象（日志 + 操作人姓名）
 */
@Data
public class OperationLogVO {

    private Long id;

    private Long userId;

    /** 操作人用户名（快照） */
    private String username;

    /** 操作人姓名（关联用户表查出来的） */
    private String realName;

    /** 所属模块 */
    private String module;

    /** 操作类型 */
    private String operation;

    /** 1 成功，0 失败 */
    private Integer status;

    /** 失败原因 */
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
