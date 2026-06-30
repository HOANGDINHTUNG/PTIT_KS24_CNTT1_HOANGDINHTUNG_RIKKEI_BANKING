package com.re.rikkei_bank_manager.user.dto.request;

import com.re.rikkei_bank_manager.common.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class UserCreateRequest {
    @NotBlank private String username;
    @NotBlank @Size(min = 6, message = "must have at least 6 characters")
    private String password;
    private String phoneNumber;
    @NotBlank @Email(message = "invalid format")
    private String email;
    @NotNull private RoleName roleName;
}
