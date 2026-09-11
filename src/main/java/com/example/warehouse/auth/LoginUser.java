package com.example.warehouse.auth;

/**
 * 当前登录用户的精简信息，存在 ThreadLocal 里，供 Service 读取操作人。
 * 不要把 passwordHash 放进来。
 */
public record LoginUser(
        Long id,
        String username,
        String displayName,
        String role
) {
}
