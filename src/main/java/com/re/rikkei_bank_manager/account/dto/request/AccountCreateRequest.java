package com.re.rikkei_bank_manager.account.dto.request;

import com.re.rikkei_bank_manager.common.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
public class AccountCreateRequest {
    @NotNull private Long userId;
    @NotNull private Currency currency;
    @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "must contain exactly 6 digits")
    private String transactionPin;
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
