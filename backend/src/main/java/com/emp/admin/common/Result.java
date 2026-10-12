package com.emp.admin.common;

import lombok.Data;

/**
 * 统一响应结果
 * <p>
 * 前后端约定：不管成功失败，HTTP 状态码都是 200，业务结果看 code。
 * 前端只需要判断 code == 200 即可。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 业务状态码，200 表示成功，见 ResultCode */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
