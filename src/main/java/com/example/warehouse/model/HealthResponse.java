package com.example.warehouse.model;

/**
 * 接口响应对象。
 * record 是 Java 16+ 提供的数据载体，适合表达不可变 DTO。
 * Spring 会通过 Jackson 将它序列化为 JSON。
 */
public record HealthResponse(String status, String application) {
}
