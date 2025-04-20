// src/main/java/com/example/tabletennis/filter/JwtAuthenticationFilter.java
package com.example.tabletennis.filter;

import com.example.tabletennis.config.JwtConfig;
import com.example.tabletennis.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 从请求头中获取 Token
        String token = jwtUtils.resolveToken(request);

        // 验证 Token 有效性
        if (token != null && jwtUtils.validateToken(token)) {
            // 将用户信息存入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(
                    jwtUtils.getAuthentication(token)
            );
        }

        filterChain.doFilter(request, response);
    }
}