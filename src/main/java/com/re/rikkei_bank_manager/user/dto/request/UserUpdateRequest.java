package com.re.rikkei_bank_manager.user.dto.request;

import com.re.rikkei_bank_manager.common.enums.RoleName;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter @Setter
public class UserUpdateRequest {
    private String phoneNumber;
    @Email(message = "invalid format")
    private String email;
    private RoleName roleName;
}
