package com.example.tabletennis.util;

import com.example.tabletennis.config.JwtConfig;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;

    // 生成 Token（保持不变）
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(jwtConfig.getSecretKey(), jwtConfig.getSignatureAlgorithm())
                .compact();
    }

    // 增强 Token 解析方法（支持WebSocket）
    public String resolveToken(Object requestObject) {
        if (requestObject instanceof HttpServletRequest) {
            // 处理HTTP请求
            HttpServletRequest request = (HttpServletRequest) requestObject;
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
        } else if (requestObject instanceof StompHeaderAccessor) {
            // 处理WebSocket STOMP消息
            StompHeaderAccessor accessor = (StompHeaderAccessor) requestObject;
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String header = authHeaders.get(0);
                if (header.startsWith("Bearer ")) {
                    return header.substring(7);
                }
            }
        }
        return null;
    }

    // 增强 Token 验证（支持详细错误处理）
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new JwtException("JWT expired", ex);
        } catch (UnsupportedJwtException ex) {
            throw new JwtException("Unsupported JWT", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtException("Malformed JWT", ex);
        } catch (Exception ex) {
            throw new JwtException("JWT validation failed", ex);
        }
    }

    // 增强认证信息获取
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

    // 私有方法：解析Claims
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}