package com.example.warehouse.config;

import com.example.warehouse.inventory.StockRecord;
import com.example.warehouse.inventory.StockRecordRepository;
import com.example.warehouse.inventory.StockType;
import com.example.warehouse.product.Product;
import com.example.warehouse.product.ProductRepository;
import com.example.warehouse.user.User;
import com.example.warehouse.user.UserRepository;
import com.example.warehouse.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 启动时写入演示数据。库里已有 admin 时跳过，避免每次重启重复插入。
 * CommandLineRunner 会在 Spring 容器启动完成后执行一次，类似前端应用挂载后的初始化。
 *
 * 演示账号：admin / admin123
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockRecordRepository stockRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(
            UserRepository userRepository,
            ProductRepository productRepository,
            StockRecordRepository stockRecordRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.stockRecordRepository = stockRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User existingAdmin = userRepository.findByUsername("admin").orElse(null);
        if (existingAdmin != null) {
            if (!UserRole.SUPER_ADMIN.equals(existingAdmin.getRole())) {
                existingAdmin.setRole(UserRole.SUPER_ADMIN);
                userRepository.save(existingAdmin);
                log.info("已将演示账号 admin 升级为超级管理员");
            }
            log.info("数据库已有数据，跳过演示数据初始化");
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setDisplayName("仓库管理员");
        admin.setRole(UserRole.SUPER_ADMIN);
        userRepository.save(admin);

        seedProduct("SKU-A001", "无线鼠标", "件", new BigDecimal("59.90"), 20);
        seedProduct("SKU-A002", "机械键盘", "件", new BigDecimal("299.00"), 8);
        seedProduct("SKU-B001", "A4 打印纸", "箱", new BigDecimal("45.00"), 50);

        log.info("演示数据已就绪，可用 admin / admin123 登录");
    }

    private void seedProduct(String sku, String name, String unit, BigDecimal price, int stock) {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setUnit(unit);
        product.setPrice(price);
        product.setStock(stock);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        productRepository.save(product);

        StockRecord record = new StockRecord();
        record.setProductId(product.getId());
        record.setSku(sku);
        record.setProductName(name);
        record.setType(StockType.IN);
        record.setQuantity(stock);
        record.setStockAfter(stock);
        record.setRemark("系统初始化入库");
        record.setOperator("系统");
        record.setCreatedAt(now);
        stockRecordRepository.save(record);
    }
}
