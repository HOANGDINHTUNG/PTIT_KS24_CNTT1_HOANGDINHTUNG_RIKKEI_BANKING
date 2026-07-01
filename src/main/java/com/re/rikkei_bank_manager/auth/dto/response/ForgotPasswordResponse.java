package com.re.rikkei_bank_manager.auth.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String resetToken;
    private LocalDateTime expiryDate;
    private String note;
}
