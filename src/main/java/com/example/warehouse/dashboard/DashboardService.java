package com.example.warehouse.dashboard;

import com.example.warehouse.inventory.StockRecord;
import com.example.warehouse.inventory.StockRecordRepository;
import com.example.warehouse.inventory.StockType;
import com.example.warehouse.product.Product;
import com.example.warehouse.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 工作台统计。数字从商品表和库存流水里算出来，不写死。
 *
 * Controller 只负责 HTTP；怎么数、怎么过滤“今天”，都放在 Service。
 */
@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final StockRecordRepository stockRecordRepository;

    public DashboardService(
            ProductRepository productRepository,
            StockRecordRepository stockRecordRepository
    ) {
        this.productRepository = productRepository;
        this.stockRecordRepository = stockRecordRepository;
    }

    public DashboardResponse load() {
        List<Product> products = productRepository.findAll();
        List<StockRecord> records = stockRecordRepository.findAll();
        LocalDate today = LocalDate.now();

        long totalStock = products.stream()
                .mapToLong(Product::getStock)
                .sum();
        long todayInQuantity = records.stream()
                .filter(record -> record.getType() == StockType.IN)
                .filter(record -> record.getCreatedAt().toLocalDate().equals(today))
                .mapToLong(StockRecord::getQuantity)
                .sum();
        long todayOutQuantity = records.stream()
                .filter(record -> record.getType() == StockType.OUT)
                .filter(record -> record.getCreatedAt().toLocalDate().equals(today))
                .mapToLong(StockRecord::getQuantity)
                .sum();

        return new DashboardResponse(
                products.size(),
                totalStock,
                records.size(),
                todayInQuantity,
                todayOutQuantity
        );
    }
}
