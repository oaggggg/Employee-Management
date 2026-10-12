package com.emp.admin.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * <p>
 * JWT 由三部分组成：Header.Payload.Signature
 * - Header：说明签名算法（HS256）
 * - Payload：业务数据（这里放 userId、username、role）和过期时间
 * - Signature：用密钥对前两部分签名，防止被篡改
 * <p>
 * 注意：JWT 只是"签名"，不是"加密"，payload 是 Base64 的，谁都能解开看到内容，
 * 所以绝对不要往里面放密码这类敏感信息。
 * <p>
 * 把角色也放进 token 的好处：拦截器做权限校验时不用再查一次数据库。
 * 代价是角色变更要等重新登录才生效，所以后台改角色/禁用账号时会主动把 Redis 里的
 * token 删掉，让旧 token 立即失效。
 */
@Component
public class JwtUtil {

    /** 签名密钥，配置在 application.yml 里 */
    @Value("${emp.jwt.secret}")
    private String secret;

    /** 过期时间（分钟） */
    @Value("${emp.jwt.expire-minutes}")
    private long expireMinutes;

    private SecretKey getSecretKey() {
        // HS256 要求密钥至少 32 字节
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 token */
    public String createToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireMinutes * 60 * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 token
     * 签名不对或已过期都会抛异常（JwtException），由调用方处理
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从解析结果中取用户 id（数字统一按字符串转，避免类型不一致） */
    public Long getUserId(Claims claims) {
        Object userId = claims.get("userId");
        return userId == null ? null : Long.valueOf(userId.toString());
    }

    /** 从解析结果中取角色 */
    public String getRole(Claims claims) {
        Object role = claims.get("role");
        return role == null ? null : role.toString();
    }

    /** 从解析结果中取用户名 */
    public String getUsername(Claims claims) {
        Object username = claims.get("username");
        return username == null ? null : username.toString();
    }

    /** token 有效期（分钟），Redis 里存登录态时用同一个值 */
    public long getExpireMinutes() {
        return expireMinutes;
    }
}
