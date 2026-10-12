package com.emp.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号列表返回对象（账号信息 + 关联的员工信息）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    /** 角色：ADMIN / EMPLOYEE */
    private String role;

    private Long employeeId;

    /** 关联员工姓名 */
    private String employeeName;

    /** 关联员工工号 */
    private String empNo;

    /** 关联员工所属部门 */
    private String deptName;

    /** 账号状态：1 启用，0 禁用 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
