package com.example.warehouse.common;

/**
 * 业务错误码。
 * 它和 HTTP 状态码是两层信息：
 * HTTP 状态码告诉浏览器/前端“这一类请求结果”（401/404/409）；
 * code 告诉业务“具体为什么失败”（密码错误还是 Token 过期）。
 */
public final class ErrorCode {

    public static final String SUCCESS = "SUCCESS";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BAD_JSON = "BAD_JSON";
    public static final String TYPE_MISMATCH = "TYPE_MISMATCH";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String FORBIDDEN = "FORBIDDEN";

    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String PRODUCT_SKU_DUPLICATE = "PRODUCT_SKU_DUPLICATE";

    public static final String STOCK_NOT_ENOUGH = "STOCK_NOT_ENOUGH";

    public static final String USERNAME_DUPLICATE = "USERNAME_DUPLICATE";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String INVALID_OLD_PASSWORD = "INVALID_OLD_PASSWORD";

    private ErrorCode() {
    }
}
