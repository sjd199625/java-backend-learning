package com.example.warehouse.config;

import com.example.warehouse.auth.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：CORS + 登录拦截器。
 *
 * 前端开发地址是 localhost:5173，后端是 8080，浏览器会视为跨域。
 * 本项目 Vite 已经把 /api 代理到 8080，浏览器看起来是同源；
 * 这里仍然配置 CORS，是为了你用 Postman 以外的页面直连 8080 时也能通。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                // 健康检查和登录必须放行，否则系统还没登录就谁都进不来。
                .excludePathPatterns("/api/health", "/api/auth/login");
    }
}
