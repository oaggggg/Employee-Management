package com.emp.admin.common;

import lombok.Getter;

/**
 * 请假类型枚举
 */
@Getter
public enum LeaveType {

    PERSONAL(1, "事假"),
    SICK(2, "病假"),
    ANNUAL(3, "年假");

    private final int code;
    private final String name;

    LeaveType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String nameOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (LeaveType type : values()) {
            if (type.code == code) {
                return type.name;
            }
        }
        return "";
    }
}
