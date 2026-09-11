package com.example.warehouse.inventory;

/**
 * 库存变动类型。枚举比字符串更安全：JSON 传错值会在绑定时失败。
 */
public enum StockType {
    IN,
    OUT
}
