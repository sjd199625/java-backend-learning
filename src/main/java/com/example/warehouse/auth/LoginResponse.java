package com.example.warehouse.auth;

/**
 * 登录成功响应。
 * token 由前端存起来，之后每次请求放到 Authorization: Bearer <token>。
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
}
