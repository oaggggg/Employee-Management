package com.emp.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 提交请假申请参数
 */
@Data
public class LeaveApplyDTO {

    @NotNull(message = "请选择请假类型")
    private Integer leaveType;

    @NotNull(message = "请选择开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    @NotNull(message = "请选择结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;

    @NotBlank(message = "请填写请假事由")
    @Size(max = 255, message = "请假事由最多 255 个字")
    private String reason;
}
