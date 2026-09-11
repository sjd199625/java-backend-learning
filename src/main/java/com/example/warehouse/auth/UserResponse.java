package com.example.warehouse.auth;

/**
 * 返回给前端的用户信息。故意没有 password / passwordHash。
 */
public record UserResponse(
        Long id,
        String username,
        String displayName,
        String role
) {
}
