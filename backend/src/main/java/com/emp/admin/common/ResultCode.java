package com.emp.admin.common;

import lombok.Getter;

/**
 * 业务状态码枚举
 * <p>
 * 用枚举代替到处写魔法数字，新增状态码只改这一处。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "数据不存在"),

    LOGIN_FAIL(1001, "用户名或密码错误"),
    ACCOUNT_LOCKED(1002, "密码错误次数过多，请10分钟后再试"),
    ACCOUNT_DISABLED(1003, "账号已被禁用，请联系管理员"),
    DATA_EXISTS(1004, "数据已存在"),
    DEPT_HAS_EMPLOYEE(1005, "该部门下还有员工，不能删除"),
    LEAVE_STATUS_ERROR(1006, "该请假单当前状态不允许此操作"),
    ACCOUNT_EXISTS(1007, "该员工已开通登录账号"),
    OLD_PASSWORD_ERROR(1008, "原密码不正确"),
    NO_EMPLOYEE_BOUND(1009, "当前账号未关联员工，无法进行该操作"),
    LEAVE_DATE_ERROR(1010, "结束日期不能早于开始日期"),

    ERROR(500, "系统繁忙，请稍后再试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
