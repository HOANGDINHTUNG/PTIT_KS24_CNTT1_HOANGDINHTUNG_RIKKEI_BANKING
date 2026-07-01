package com.re.rikkei_bank_manager.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank
    @Email(message = "invalid format")
    @Schema(example = "nguyenvana@gmail.com")
    private String email;
}
