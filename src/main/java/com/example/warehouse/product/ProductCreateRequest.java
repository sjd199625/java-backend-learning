package com.example.warehouse.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 新增商品的请求体。库存不在这里改，新建商品默认库存为 0，
 * 入库/出库走独立接口，避免“改商品资料”和“改库存”混在一个接口里。
 */
public record ProductCreateRequest(
        @NotBlank(message = "商品编号不能为空")
        @Size(max = 32, message = "商品编号最多32个字符")
        String sku,

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
