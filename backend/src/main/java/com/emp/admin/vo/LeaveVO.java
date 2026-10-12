package com.emp.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假单返回对象（请假单 + 申请人信息 + 审批人信息）
 */
@Data
public class LeaveVO {

    private Long id;

    private Long employeeId;

    private Integer leaveType;

    /** 请假类型名称：事假 / 病假 / 年假 */
    private String leaveTypeName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;

    private Integer days;

    private String reason;

    private Integer status;

    /** 状态名称：待审批 / 已通过 / 已驳回 / 已撤销 */
    private String statusName;

    private Long approveUserId;

    /** 审批人姓名 */
    private String approveUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime approveTime;

    private String approveRemark;

    /** 申请人姓名 */
    private String employeeName;

    /** 申请人工号 */
    private String empNo;

    /** 申请人所属部门 */
    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
