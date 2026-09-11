package com.example.warehouse.inventory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class StockRecordRepository {

    private static final RowMapper<StockRecord> ROW_MAPPER = (rs, rowNum) -> {
        StockRecord record = new StockRecord();
        record.setId(rs.getLong("id"));
        record.setProductId(rs.getLong("product_id"));
        record.setSku(rs.getString("sku"));
        record.setProductName(rs.getString("product_name"));
        record.setType(StockType.valueOf(rs.getString("type")));
        record.setQuantity(rs.getInt("quantity"));
        record.setStockAfter(rs.getInt("stock_after"));
        record.setRemark(rs.getString("remark"));
        record.setOperator(rs.getString("operator"));
        record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return record;
    };

    private final JdbcTemplate jdbc;

    public StockRecordRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public StockRecord save(StockRecord record) {
        if (record.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        """
                        INSERT INTO stock_records
                            (product_id, sku, product_name, type, quantity, stock_after, remark, operator, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, record.getProductId());
                ps.setString(2, record.getSku());
                ps.setString(3, record.getProductName());
                ps.setString(4, record.getType().name());
                ps.setInt(5, record.getQuantity());
                ps.setInt(6, record.getStockAfter());
                ps.setString(7, record.getRemark() == null ? "" : record.getRemark());
                ps.setString(8, record.getOperator());
                ps.setTimestamp(9, Timestamp.valueOf(record.getCreatedAt()));
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                record.setId(key.longValue());
            }
            return record;
        }

        jdbc.update(
                """
                UPDATE stock_records
                SET product_id = ?, sku = ?, product_name = ?, type = ?, quantity = ?,
                    stock_after = ?, remark = ?, operator = ?, created_at = ?
                WHERE id = ?
                """,
                record.getProductId(),
                record.getSku(),
                record.getProductName(),
                record.getType().name(),
                record.getQuantity(),
                record.getStockAfter(),
                record.getRemark(),
                record.getOperator(),
                Timestamp.valueOf(record.getCreatedAt()),
                record.getId()
        );
        return record;
    }

    public List<StockRecord> findAll() {
        return jdbc.query(
                """
                SELECT id, product_id, sku, product_name, type, quantity, stock_after, remark, operator, created_at
                FROM stock_records
                ORDER BY id DESC
                """,
                ROW_MAPPER
        );
    }
}
