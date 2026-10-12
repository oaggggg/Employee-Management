package com.emp.admin.vo;

import lombok.Data;

/**
 * 返回给前端的用户信息（只包含需要的字段，密码不带出去）
 */
@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String realName;

    private String avatar;

    /** 角色：ADMIN 管理员，EMPLOYEE 普通员工（前端据此显示菜单） */
    private String role;
}
