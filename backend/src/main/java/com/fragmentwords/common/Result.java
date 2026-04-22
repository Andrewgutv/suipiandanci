package com.fragmentwords.common;

import lombok.Data;

@Data
public class Result<T> {
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_FORBIDDEN = 403;
    public static final int CODE_NOT_FOUND = 404;
    public static final int CODE_CONFLICT = 409;
    public static final int CODE_INTERNAL_ERROR = 500;

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> badRequest(String message) {
        Result<T> result = new Result<>();
        result.setCode(CODE_BAD_REQUEST);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.setCode(CODE_UNAUTHORIZED);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> notFound(String message) {
        Result<T> result = new Result<>();
        result.setCode(CODE_NOT_FOUND);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<>();
        result.setCode(CODE_FORBIDDEN);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> conflict(String message) {
        Result<T> result = new Result<>();
        result.setCode(CODE_CONFLICT);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
}
