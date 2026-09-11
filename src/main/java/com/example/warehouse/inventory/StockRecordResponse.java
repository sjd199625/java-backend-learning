package com.example.warehouse.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record StockRecordResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        StockType type,
        int quantity,
        int stockAfter,
        String remark,
        String operator,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {

    public static StockRecordResponse from(StockRecord record) {
        return new StockRecordResponse(
                record.getId(),
                record.getProductId(),
                record.getSku(),
                record.getProductName(),
                record.getType(),
                record.getQuantity(),
                record.getStockAfter(),
                record.getRemark(),
                record.getOperator(),
                record.getCreatedAt()
        );
    }
}
