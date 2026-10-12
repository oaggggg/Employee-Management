package com.emp.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工表
 * <p>
 * 说明：为了控制代码复杂度，这里直接拿实体接收前端参数（也顺便当 DTO 用）。
 * 真实项目里更规范的做法是单独建 EmployeeDTO / EmployeeVO，和数据库表解耦，
 * 这一点在面试时可以作为"我知道的改进点"主动说出来。
 */
@Data
@TableName("sys_employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工号，唯一 */
    @NotBlank(message = "工号不能为空")
    private String empNo;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /** 性别：1 男，2 女 */
    private Integer gender;

    private Integer age;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String email;

    /** 所属部门 id（逻辑外键，实际用 join 查部门名称） */
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    /** 职位 */
    private String position;

    /** 薪资 */
    private BigDecimal salary;

    /** 入职日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate hireDate;

    /** 状态：1 在职，0 离职 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer deleted;

    /** 新增员工时是否同时开通登录账号（不落库，只用来接收前端参数） */
    @TableField(exist = false)
    private Boolean openAccount;

    /** 开通账号时的角色：ADMIN 管理员，EMPLOYEE 普通员工（不落库） */
    @TableField(exist = false)
    private String accountRole;
}
