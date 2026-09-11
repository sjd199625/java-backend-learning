package com.example.warehouse.common;

import java.util.List;

/**
 * 分页响应。
 * page 从 1 开始，和 Ant Design Table 的 current 一致，避免前端再减一。
 */
public record PageResult<T>(List<T> items, long total, int page, int size) {
}
