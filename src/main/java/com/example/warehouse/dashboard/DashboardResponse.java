package com.example.warehouse.dashboard;

/**
 * 工作台返回给前端的 data 结构。
 * record 组件名必须和前端 Dashboard 类型字段名一致，Jackson 才会正确序列化成 JSON。
 */
public record DashboardResponse(
        long productCount,
        long totalStock,
        long recordCount,
        long todayInQuantity,
        long todayOutQuantity
) {
}
