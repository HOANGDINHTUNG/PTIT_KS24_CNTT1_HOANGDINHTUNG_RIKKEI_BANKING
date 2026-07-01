package com.re.rikkei_bank_manager.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank
    @Schema(example = "OldPassword@123")
    private String oldPassword;

    @NotBlank
    @Size(min = 6, max = 50, message = "Must be between 6 and 50 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$", message = "Must contain at least one uppercase letter, lowercase letter, and number")
    @Schema(example = "NewPassword@123")
    private String newPassword;
}
