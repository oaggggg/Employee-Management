package com.emp.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 个人中心返回对象：账号信息 + 关联的员工资料
 */
@Data
public class ProfileVO {

    private Long id;

    private String username;

    private String realName;

    private String role;

    /** 关联的员工 id，为空表示该账号没有绑定员工 */
    private Long employeeId;

    private String empNo;

    private String deptName;

    private String position;

    private String phone;

    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate hireDate;

    /** 员工在职状态：1 在职，0 离职 */
    private Integer employeeStatus;
}
