package com.example.warehouse.user;

/**
 * 用户实体。对应 MySQL 表 users，可以包含 passwordHash。
 * 返回给前端时必须转成 UserResponse，不能直接序列化实体，否则会泄露密码哈希。
 */
public class User {

    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private String role;

    public User() {
    }

    public User(Long id, String username, String passwordHash, String displayName, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
