package com.re.rikkei_bank_manager.auth.mapper;

import com.re.rikkei_bank_manager.auth.dto.response.AuthResponse;
import com.re.rikkei_bank_manager.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public AuthResponse toResponse(User user, String accessToken, String refreshToken, long expiresInMs) {
        if (user == null) return null;
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(expiresInMs)
                .userId(user.getId())
                .username(user.getUsername())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }
}
