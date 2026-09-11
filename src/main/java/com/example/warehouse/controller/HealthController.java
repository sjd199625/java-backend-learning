package com.example.warehouse.controller;

import com.example.warehouse.common.Result;
import com.example.warehouse.model.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查，不需要登录。
 * 前端和运维都可以靠它判断后端进程是否还活着。
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     * 现在和其他接口一样包在 Result 里，方便前端统一解析 code/message/data。
     */
    @GetMapping("/health")
    public Result<HealthResponse> health() {
        return Result.success(new HealthResponse("UP", "java-backend-learning"));
    }
}
