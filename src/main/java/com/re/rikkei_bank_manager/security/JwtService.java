package com.re.rikkei_bank_manager.security;

import com.re.rikkei_bank_manager.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.access-token-expiration-ms}") private long accessTokenExpirationMs;
    @Value("${app.jwt.refresh-token-expiration-ms}") private long refreshTokenExpirationMs;

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName().name());
        claims.put("userId", user.getId());
        return buildToken(claims, user.getUsername(), accessTokenExpirationMs);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        claims.put("userId", user.getId());
        return buildToken(claims, user.getUsername(), refreshTokenExpirationMs);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration); }

    public LocalDateTime extractExpirationAsLocalDateTime(String token) {
        return extractExpiration(token).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public long getAccessTokenExpirationMs() { return accessTokenExpirationMs; }
    public long getRefreshTokenExpirationMs() { return refreshTokenExpirationMs; }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) { return extractExpiration(token).before(new Date()); }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
