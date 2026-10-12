package com.emp.admin.common;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 业务校验不通过时直接 throw，由全局异常处理器统一转成返回给前端的格式，
 * 这样 Service 层不用写一堆 if-else 判断返回码。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /** 需要用自定义提示语时使用 */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
