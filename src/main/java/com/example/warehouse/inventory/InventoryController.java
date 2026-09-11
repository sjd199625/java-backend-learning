package com.example.warehouse.inventory;

import com.example.warehouse.common.PageResult;
import com.example.warehouse.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出入库接口。
 *
 * 为什么入库、出库分成两个 URL，而不是一个接口传 type=IN/OUT？
 * 两个 URL 的意图更清晰，也方便以后给出库单独加审批、幂等键等规则。
 */
@Validated
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/in")
    public ResponseEntity<Result<StockRecordResponse>> stockIn(
            @Valid @RequestBody StockChangeRequest request
    ) {
        StockRecordResponse record = inventoryService.stockIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success("入库成功", record));
    }

    @PostMapping("/out")
    public ResponseEntity<Result<StockRecordResponse>> stockOut(
            @Valid @RequestBody StockChangeRequest request
    ) {
        StockRecordResponse record = inventoryService.stockOut(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success("出库成功", record));
    }

    @GetMapping("/records")
    public Result<PageResult<StockRecordResponse>> records(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page 从 1 开始") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size 至少为 1")
            @Max(value = 100, message = "size 不能超过 100") Integer size
    ) {
        return Result.success(inventoryService.listRecords(productId, page, size));
    }
}
