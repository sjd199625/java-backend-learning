package com.example.warehouse.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求 DTO。
 * 只接收前端需要提交的字段。不要把实体 User 直接拿来接 JSON，
 * 否则客户端可能多传 id、role、passwordHash 等不该由前端决定的字段。
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 32, message = "用户名最多32个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需要在6到64个字符之间")
        String password
) {
}
