package com.example.warehouse.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 入库/出库共用一个请求体，用 URL 区分方向：
 * POST /api/inventory/in  和 POST /api/inventory/out。
 */
public record StockChangeRequest(
        @NotNull(message = "商品 ID 不能为空")
        Long productId,

        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于 0")
        Integer quantity,

        @Size(max = 128, message = "备注最多128个字符")
        String remark
) {
}
