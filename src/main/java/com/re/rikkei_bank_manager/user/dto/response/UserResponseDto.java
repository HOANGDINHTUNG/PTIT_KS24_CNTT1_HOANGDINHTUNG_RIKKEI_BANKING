package com.re.rikkei_bank_manager.user.dto.response;

import com.re.rikkei_bank_manager.common.enums.RoleName;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String phoneNumber;
    private String email;
    private boolean active;
    private boolean kyc;
    private LocalDateTime createdAt;
    private RoleName roleName;
}
