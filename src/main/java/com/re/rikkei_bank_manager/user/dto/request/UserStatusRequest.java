package com.re.rikkei_bank_manager.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
public class UserStatusRequest {
    @NotNull private Boolean active;
}
