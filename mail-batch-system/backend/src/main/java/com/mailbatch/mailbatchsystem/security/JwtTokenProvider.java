package com.mailbatch.mailbatchsystem.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // 直接从环境变量或配置文件读密钥，确保一致
    private static final String JWT_SECRET = System.getenv("JWT_SECRET") != null 
            ? System.getenv("JWT_SECRET") 
            : "your-very-secure-secret-key-should-be-at-least-256-bits-long";
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    private static final String AUTHORITIES_KEY = "roles";

    /**
     * 生成 JWT Token(标准方式,供 JwtAuthenticationFilter 使用)
     */
    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .id(userPrincipal.getUsername())
                .subject(userPrincipal.getUsername())
                .claim(AUTHORITIES_KEY, authorities)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 为用户生成 JWT Token(供 SimpleAuthController 使用)
     */
    public String generateTokenForUser(com.mailbatch.mailbatchsystem.entity.User user) {
        return Jwts.builder()
                .id(user.getUsername())
                .subject(user.getUsername())
                .claim(AUTHORITIES_KEY, user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("无效的 JWT Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的 JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT Token 为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从 Token 中获取认证信息
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        String roles = claims.get(AUTHORITIES_KEY, String.class);

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(username, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        // 使用固定密钥，确保生成和验证一致
        String secret = JWT_SECRET;
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
