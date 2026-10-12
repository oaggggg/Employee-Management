package com.emp.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 开通登录账号参数
 */
@Data
public class CreateUserDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String realName;

    @NotBlank(message = "请选择角色")
    @Pattern(regexp = "^(ADMIN|MANAGER|EMPLOYEE)$", message = "角色只能是 ADMIN、MANAGER 或 EMPLOYEE")
    private String role;

    /** 关联的员工 id，可以为空（比如再建一个不绑定员工的管理员账号） */
    private Long employeeId;

    /** 初始密码，不传则使用默认密码 123456 */
    private String password;
}
