package com.emp.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 请假审批参数
 */
@Data
public class LeaveApproveDTO {

    /** 审批结果：1 通过，2 驳回 */
    @NotNull(message = "请选择审批结果")
    @Min(value = 1, message = "审批结果只能是 1 通过或 2 驳回")
    @Max(value = 2, message = "审批结果只能是 1 通过或 2 驳回")
    private Integer status;

    @Size(max = 255, message = "审批意见最多 255 个字")
    private String approveRemark;
}
