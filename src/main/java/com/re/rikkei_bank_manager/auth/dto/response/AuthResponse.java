package com.re.rikkei_bank_manager.auth.dto.response;

import com.re.rikkei_bank_manager.common.enums.RoleName;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresInMs;
    private Long userId;
    private String username;
    private RoleName roleName;
}
