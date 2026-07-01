package com.re.rikkei_bank_manager.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class LoginRequest {
    @NotBlank
    @Schema(example = "nguyenvana")
    private String username;

    @NotBlank
    @Schema(example = "Password@123")
    private String password;
}
