package com.emp.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改状态参数（启用 / 禁用）
 */
@Data
public class UpdateStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
