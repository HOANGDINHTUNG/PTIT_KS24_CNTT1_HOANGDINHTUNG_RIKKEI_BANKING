package com.re.rikkei_bank_manager.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    @Pattern(regexp = "^\\S+$", message = "Username cannot contain spaces")
    @Schema(example = "nguyenvana")
    private String username;

    @NotBlank
    @Size(min = 6, max = 50, message = "Must be between 6 and 50 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$", message = "Must contain at least one uppercase letter, lowercase letter, and number")
    @Schema(example = "Password@123")
    private String password;

    @NotBlank
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid Vietnamese phone number format")
    @Schema(example = "0901234567")
    private String phoneNumber;

    @NotBlank
    @Email
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    @Schema(example = "nguyenvana@gmail.com")
    private String email;
}
