package com.re.rikkei_bank_manager.account.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
public class AccountStatusRequest {
    @NotNull private Boolean active;
}
