package com.example.warehouse.dashboard;

import com.example.warehouse.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台接口。
 *
 * 构造器里的 DashboardService 由 Spring 注入，不要在 Controller 里 new。
 * GET /api/dashboard 需要登录。
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Result<DashboardResponse> dashboard() {
        return Result.success(dashboardService.load());
    }
}
