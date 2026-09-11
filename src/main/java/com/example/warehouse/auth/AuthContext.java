package com.example.warehouse.auth;

import com.example.warehouse.common.BusinessException;

/**
 * 用 ThreadLocal 保存“这次请求是谁”。
 *
 * HTTP 是无状态的，每个请求可能在不同线程处理。
 * Interceptor 解析 JWT 后 set，Controller/Service 随时 get，
 * afterCompletion 里必须 remove，避免 Tomcat 线程复用时串到下一个请求。
 */
public final class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser requireUser() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
