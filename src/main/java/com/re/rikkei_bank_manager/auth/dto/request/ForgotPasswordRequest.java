package com.re.rikkei_bank_manager.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank
    @Email(message = "invalid format")
    private String email;
}
