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
 * 操作日志（审计留痕）
 * <p>
 * 只记录最关键的审计信息：谁、什么时候、在哪个模块、做了什么操作、成功还是失败。
 * username 存的是操作发生时的快照：就算以后账号被删掉，操作痕迹依然能查。
 */
@Data
@TableName("sys_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人（系统用户 id） */
    private Long userId;

    /** 操作人用户名（快照） */
    private String username;

    /** 所属模块：部门管理 / 员工管理 / 账号管理 / 请假管理 / 个人中心 */
    private String module;

    /** 操作类型：新增部门、修改员工、删除账号、审批请假等 */
    private String operation;

    /** 结果：1 成功，0 失败 */
    private Integer status;

    /** 失败原因（成功时为空） */
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
