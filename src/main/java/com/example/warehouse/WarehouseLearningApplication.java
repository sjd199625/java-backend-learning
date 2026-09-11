package com.example.warehouse;

import com.example.warehouse.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 项目启动入口。
 *
 * @SpringBootApplication 主要完成三件事：
 * 1. 声明这是一个 Spring Boot 配置类；
 * 2. 启用自动配置；
 * 3. 扫描当前包及其子包中的 Controller、Service、Interceptor 等组件。
 *
 * 请求进入后的大致链路：
 * HTTP → Tomcat → CORS/Filter → AuthInterceptor（校验 JWT）
 * → DispatcherServlet 路由到 Controller
 * → 参数绑定与 @Valid 校验
 * → Service 业务规则
 * → Repository JDBC 读写 MySQL
 * → 返回 DTO → Jackson 序列化成 JSON。
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class WarehouseLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseLearningApplication.class, args);
    }
}
