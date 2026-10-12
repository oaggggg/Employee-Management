package com.emp.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户表（登录账号）
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 加密后的密码，绝对不能明文存 */
    @JsonIgnore
    private String password;

    private String realName;

    /** 头像地址 */
    private String avatar;

    /** 角色：ADMIN 管理员，EMPLOYEE 普通员工 */
    private String role;

    /** 关联的员工 id，为空表示这个账号不绑定员工（比如系统管理员） */
    private Long employeeId;

    /** 状态：1 启用，0 禁用 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0 未删除，1 已删除（加了 @TableLogic 后查询会自动带上 deleted = 0） */
    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
