package com.re.rikkei_bank_manager.user.dto.request;

import com.re.rikkei_bank_manager.common.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class UserCreateRequest {
    @NotBlank
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    @Pattern(regexp = "^\\S+$", message = "Username cannot contain spaces")
    private String username;

    @NotBlank
    @Size(min = 6, max = 50, message = "Must be between 6 and 50 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$", message = "Must contain at least one uppercase letter, lowercase letter, and number")
    private String password;

    @NotBlank
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid Vietnamese phone number format")
    private String phoneNumber;

    @NotBlank
    @Email(message = "Invalid format")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;

    @NotNull
    private RoleName roleName;
}
