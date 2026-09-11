package com.example.warehouse.inventory;

import com.example.warehouse.auth.AuthContext;
import com.example.warehouse.common.BusinessException;
import com.example.warehouse.common.ErrorCode;
import com.example.warehouse.common.PageResult;
import com.example.warehouse.product.Product;
import com.example.warehouse.product.ProductRepository;
import com.example.warehouse.product.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存核心业务。
 *
 * 出库不是简单的 stock - n：
 * 1. 商品必须存在；
 * 2. 扣减后不能为负；
 * 3. 改库存和写流水要一起完成，否则对不上账。
 *
 * synchronized 按商品 ID 加锁，避免同一商品并发出入库把库存算乱。
 * 改库存和写流水包在同一事务里，失败会一起回滚。
 */
@Service
public class InventoryService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final StockRecordRepository stockRecordRepository;
    private final Map<Long, Object> productLocks = new ConcurrentHashMap<>();

    public InventoryService(
            ProductService productService,
            ProductRepository productRepository,
            StockRecordRepository stockRecordRepository
    ) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.stockRecordRepository = stockRecordRepository;
    }

    @Transactional
    public StockRecordResponse stockIn(StockChangeRequest request) {
        return changeStock(request, StockType.IN);
    }

    @Transactional
    public StockRecordResponse stockOut(StockChangeRequest request) {
        return changeStock(request, StockType.OUT);
    }

    public PageResult<StockRecordResponse> listRecords(Long productId, int page, int size) {
        List<StockRecordResponse> all = stockRecordRepository.findAll().stream()
                .filter(record -> productId == null || record.getProductId().equals(productId))
                .map(StockRecordResponse::from)
                .toList();
        int fromIndex = Math.min((page - 1) * size, all.size());
        int toIndex = Math.min(fromIndex + size, all.size());
        return new PageResult<>(all.subList(fromIndex, toIndex), all.size(), page, size);
    }

    private StockRecordResponse changeStock(StockChangeRequest request, StockType type) {
        Object lock = productLocks.computeIfAbsent(request.productId(), id -> new Object());
        synchronized (lock) {
            Product product = productService.requireProduct(request.productId());
            int delta = type == StockType.IN ? request.quantity() : -request.quantity();
            int nextStock = product.getStock() + delta;
            if (nextStock < 0) {
                throw BusinessException.conflict(
                        ErrorCode.STOCK_NOT_ENOUGH,
                        "库存不足，当前库存 " + product.getStock() + "，无法出库 " + request.quantity()
                );
            }

            product.setStock(nextStock);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            StockRecord record = new StockRecord();
            record.setProductId(product.getId());
            record.setSku(product.getSku());
            record.setProductName(product.getName());
            record.setType(type);
            record.setQuantity(request.quantity());
            record.setStockAfter(nextStock);
            record.setRemark(request.remark() == null ? "" : request.remark().trim());
            record.setOperator(AuthContext.requireUser().displayName());
            record.setCreatedAt(LocalDateTime.now());
            return StockRecordResponse.from(stockRecordRepository.save(record));
        }
    }
}
