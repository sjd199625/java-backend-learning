package com.example.warehouse.common;

/**
 * 所有接口的统一响应外壳。
 *
 * 前端可以固定按 { code, message, data } 解析，不用每个接口一套结构。
 * 注意：统一响应不能替代 HTTP 状态码。商品不存在时仍应返回 404，
 * 同时 body 里给出 PRODUCT_NOT_FOUND。
 *
 * @param <T> data 的类型，失败时一般为 null
 */
public record Result<T>(String code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS, message, data);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(code, message, null);
    }
}
