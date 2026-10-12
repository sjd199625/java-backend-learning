package com.example.warehouse.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * 用户仓储。读写 MySQL 表 users。
 * Controller 和 Service 仍然只依赖这些方法。
 *
 * @Repository 让 Spring 把它注册成 Bean，并能被 Service 注入。
 */
@Repository
public class UserRepository {

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setDisplayName(rs.getString("display_name"));
        user.setRole(rs.getString("role"));
        return user;
    };

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User save(User user) {
        if (user.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        """
                        INSERT INTO users (username, password_hash, display_name, role)
                        VALUES (?, ?, ?, ?)
                        """,
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getDisplayName());
                ps.setString(4, user.getRole());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                user.setId(key.longValue());
            }
            return user;
        }

        jdbc.update(
                """
                UPDATE users
                SET username = ?, password_hash = ?, display_name = ?, role = ?
                WHERE id = ?
                """,
                user.getUsername(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getRole(),
                user.getId()
        );
        return user;
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = jdbc.query(
                "SELECT id, username, password_hash, display_name, role FROM users WHERE username = ?",
                ROW_MAPPER,
                username
        );
        return users.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbc.query(
                "SELECT id, username, password_hash, display_name, role FROM users WHERE id = ?",
                ROW_MAPPER,
                id
        );
        return users.stream().findFirst();
    }

    public List<User> findAll() {
        return jdbc.query(
                "SELECT id, username, password_hash, display_name, role FROM users ORDER BY id",
                ROW_MAPPER
        );
    }
}
