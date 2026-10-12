package com.emp.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置密码参数
 * <p>
 * 不传密码时后端默认重置为 123456。
 */
@Data
public class ResetPasswordDTO {

    @Size(min = 6, max = 20, message = "新密码长度需为 6-20 位")
    private String password;
}
