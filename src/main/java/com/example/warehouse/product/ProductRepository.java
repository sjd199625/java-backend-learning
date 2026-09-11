package com.example.warehouse.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 商品仓储：CRUD 的数据入口。
 * 读写 MySQL 表 products。Controller 和 Service 仍然只依赖这些方法。
 */
@Repository
public class ProductRepository {

    private static final RowMapper<Product> ROW_MAPPER = (rs, rowNum) -> {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setSku(rs.getString("sku"));
        product.setName(rs.getString("name"));
        product.setUnit(rs.getString("unit"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    };

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        """
                        INSERT INTO products (sku, name, unit, price, stock, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, product.getSku());
                ps.setString(2, product.getName());
                ps.setString(3, product.getUnit());
                ps.setBigDecimal(4, product.getPrice());
                ps.setInt(5, product.getStock());
                ps.setTimestamp(6, Timestamp.valueOf(product.getCreatedAt()));
                ps.setTimestamp(7, Timestamp.valueOf(product.getUpdatedAt()));
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                product.setId(key.longValue());
            }
            return product;
        }

        jdbc.update(
                """
                UPDATE products
                SET name = ?, unit = ?, price = ?, stock = ?, updated_at = ?
                WHERE id = ?
                """,
                product.getName(),
                product.getUnit(),
                product.getPrice(),
                product.getStock(),
                Timestamp.valueOf(product.getUpdatedAt()),
                product.getId()
        );
        return product;
    }

    public Optional<Product> findById(Long id) {
        List<Product> products = jdbc.query(
                "SELECT id, sku, name, unit, price, stock, created_at, updated_at FROM products WHERE id = ?",
                ROW_MAPPER,
                id
        );
        return products.stream().findFirst();
    }

    public Optional<Product> findBySku(String sku) {
        List<Product> products = jdbc.query(
                "SELECT id, sku, name, unit, price, stock, created_at, updated_at FROM products WHERE LOWER(sku) = LOWER(?)",
                ROW_MAPPER,
                sku
        );
        return products.stream().findFirst();
    }

    public boolean existsBySku(String sku) {
        return findBySku(sku).isPresent();
    }

    public List<Product> findAll() {
        return jdbc.query(
                "SELECT id, sku, name, unit, price, stock, created_at, updated_at FROM products ORDER BY id DESC",
                ROW_MAPPER
        );
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM products WHERE id = ?", id);
    }

    public long count() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM products", Long.class);
        return count == null ? 0 : count;
    }
}
