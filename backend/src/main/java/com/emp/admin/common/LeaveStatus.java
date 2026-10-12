package com.emp.admin.common;

import lombok.Getter;

/**
 * 请假单状态枚举
 * <p>
 * 状态流转：0 待审批 →（管理员审批）→ 1 已通过 / 2 已驳回
 * 0 待审批 →（本人撤销）→ 3 已撤销
 * 只有"待审批"状态才允许审批或撤销，这一点在 Service 里会做校验。
 */
@Getter
public enum LeaveStatus {

    PENDING(0, "待审批"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回"),
    CANCELED(3, "已撤销");

    private final int code;
    private final String name;

    LeaveStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /** 根据状态码取中文名称，给前端展示用 */
    public static String nameOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (LeaveStatus status : values()) {
            if (status.code == code) {
                return status.name;
            }
        }
        return "";
    }
}
