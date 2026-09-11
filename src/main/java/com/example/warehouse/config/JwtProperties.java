package com.example.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 读取 application.yml 里 warehouse.jwt 配置。
 * 比把密钥写死在 Java 代码里更好改，也方便以后改成环境变量。
 */
@ConfigurationProperties(prefix = "warehouse.jwt")
public class JwtProperties {

    private String secret;
    private long expireSeconds = 86400;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}
