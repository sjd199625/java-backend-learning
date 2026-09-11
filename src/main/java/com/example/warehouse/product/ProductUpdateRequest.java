package com.example.warehouse.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 修改商品资料。sku 不允许改，避免历史出入库记录对不上编号。
 */
public record ProductUpdateRequest(
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 64, message = "商品名称最多64个字符")
        String name,

        @NotBlank(message = "单位不能为空")
        @Size(max = 16, message = "单位最多16个字符")
        String unit,

        @NotNull(message = "单价不能为空")
        @DecimalMin(value = "0.00", message = "单价不能为负数")
        BigDecimal price
) {
}
