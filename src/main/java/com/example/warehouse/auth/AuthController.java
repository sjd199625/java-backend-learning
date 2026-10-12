package com.example.warehouse.auth;

import com.example.warehouse.common.Result;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 认证接口。
 *
 * POST /api/auth/login  不需要 Token，用 JSON Body 接收用户名密码。
 * GET  /api/auth/me     需要 Token，演示从 Header 识别当前用户。
 * POST /api/auth/logout 需要 Token，把当前 Token 加入黑名单。
 *
 * @RestController = @Controller + @ResponseBody，方法返回值直接写成 JSON。
 * @RequestMapping("/api/auth") 是这一组接口的公共前缀。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * @Valid 会在进入方法前校验 LoginRequest 上的注解。
     * 失败时不会执行 login()，而是由 GlobalExceptionHandler 返回 400。
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserResponse> me() {
        return Result.success(authService.currentUser());
    }

    /**
     * @RequestHeader 从 HTTP Header 取值，对应前端的 Authorization。
     * required = false 是为了让拦截器先处理“没带 Token”的情况。
     */
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        authService.logout(authorization);
        return Result.success("已退出登录", null);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }
}
