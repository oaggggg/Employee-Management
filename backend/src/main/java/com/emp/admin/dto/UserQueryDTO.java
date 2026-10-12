package com.emp.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 账号列表查询条件
 */
@Data
public class UserQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    /** 用户名，模糊查询 */
    private String username;

    /** 角色：ADMIN / MANAGER / EMPLOYEE */
    private String role;

    /** 员工 id 集合：部门负责人只能看本部门员工的账号（由后端填充） */
    private List<Long> employeeIds;
}
