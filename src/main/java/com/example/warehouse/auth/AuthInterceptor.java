package com.example.warehouse.auth;

import com.example.warehouse.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器。在 Controller 方法执行之前运行。
 *
 * 对比前端：相当于 axios 请求拦截器的服务端版本。
 * 没带 Token、Token 过期或被注销，都直接抛 401，不会进入业务代码。
 *
 * OPTIONS 预检请求不能拦，否则浏览器 CORS 会失败。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            throw BusinessException.unauthorized("请先登录");
        }
        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw BusinessException.unauthorized("Authorization 格式应为 Bearer <token>");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw BusinessException.unauthorized("请先登录");
        }

        LoginUser loginUser = jwtService.parseToken(token);
        AuthContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        AuthContext.clear();
    }
}
