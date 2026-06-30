package com.re.rikkei_bank_manager.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class ResetPasswordRequest {
    @NotBlank private String resetToken;
    @NotBlank @Size(min = 6, message = "must have at least 6 characters")
    private String newPassword;
}
