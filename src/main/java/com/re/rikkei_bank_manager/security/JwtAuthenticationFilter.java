package com.re.rikkei_bank_manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.rikkei_bank_manager.common.response.ErrorResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@Component @RequiredArgsConstructor @Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (!org.springframework.util.StringUtils.hasText(header) || !org.springframework.util.StringUtils.startsWithIgnoreCase(header, "Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7);
        try {
            boolean isRevoked = false;
            try {
                isRevoked = Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
            } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
                log.warn("Redis is unreachable, skipping token blacklist check.");
            }
            
            if (isRevoked) {
                sendErrorResponse(res, req.getRequestURI(), "Token has been revoked");
                return;
            }
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails details = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, details)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            details, null, details.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            chain.doFilter(req, res);
        } catch (JwtException | IllegalArgumentException ex) {
            sendErrorResponse(res, req.getRequestURI(), "Invalid JWT token");
        }
    }

    private void sendErrorResponse(HttpServletResponse res, String path, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(401)
                .error("Unauthorized")
                .message(message)
                .path(path)
                .build());
    }
}
