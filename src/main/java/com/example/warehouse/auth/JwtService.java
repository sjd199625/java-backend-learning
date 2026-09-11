package com.example.warehouse.auth;

import com.example.warehouse.common.BusinessException;
import com.example.warehouse.config.JwtProperties;
import com.example.warehouse.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 工具。
 *
 * JWT 是一段被签名的 JSON，里面可以放 userId、username、过期时间。
 * 前端带着它访问接口，服务端用同一把密钥验签，就能认出是谁，而不用在服务端保存 Session。
 *
 * 学习阶段把注销的 Token 放进内存黑名单。进程重启后黑名单会清空，
 * 未过期的旧 Token 仍可用。正式环境通常把黑名单放到 Redis，或改用更短过期时间。
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey secretKey;
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getExpireSeconds());
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("displayName", user.getDisplayName())
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public LoginUser parseToken(String token) {
        if (blacklist.contains(token)) {
            throw BusinessException.unauthorized("登录已失效，请重新登录");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new LoginUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class),
                    claims.get("displayName", String.class),
                    claims.get("role", String.class)
            );
        } catch (ExpiredJwtException exception) {
            throw BusinessException.unauthorized("登录已过期，请重新登录");
        } catch (RuntimeException exception) {
            throw BusinessException.unauthorized("登录凭证无效，请重新登录");
        }
    }

    public void blacklist(String token) {
        blacklist.add(token);
    }

    public long getExpireSeconds() {
        return properties.getExpireSeconds();
    }
}
