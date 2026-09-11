package com.example.warehouse.common;

import org.springframework.http.HttpStatus;

/**
 * 业务异常。Service 发现“商品不存在、库存不足”等情况时抛出它，
 * 不要在 Controller 里到处写 if/return。
 *
 * 由 {@link GlobalExceptionHandler} 统一转成 HTTP 状态码 + Result JSON。
 * 这样前端永远能拿到结构一致的错误体。
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(ErrorCode.UNAUTHORIZED, message, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException invalidCredentials() {
        return new BusinessException(
                ErrorCode.INVALID_CREDENTIALS,
                "用户名或密码错误",
                HttpStatus.UNAUTHORIZED
        );
    }

    public static BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String code, String message) {
        return new BusinessException(code, message, HttpStatus.CONFLICT);
    }
}
