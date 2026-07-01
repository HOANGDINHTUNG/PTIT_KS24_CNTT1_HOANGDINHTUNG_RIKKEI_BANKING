package com.re.rikkei_bank_manager.user.dto.request;

import com.re.rikkei_bank_manager.common.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
public class UserUpdateRequest {
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid Vietnamese phone number format")
    private String phoneNumber;

    @Email(message = "Invalid format")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;

    private RoleName roleName;
}
