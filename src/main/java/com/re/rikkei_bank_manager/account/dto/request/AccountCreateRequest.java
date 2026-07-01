package com.re.rikkei_bank_manager.account.dto.request;

import com.re.rikkei_bank_manager.common.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class AccountCreateRequest {
    @NotNull 
    @Schema(example = "1")
    private Long userId;

    @NotNull 
    @Schema(example = "VND")
    private Currency currency;

    @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Must contain exactly 6 digits")
    @Schema(example = "740945")
    private String transactionPin;

    @DecimalMin(value = "0.00", inclusive = true)
    @Schema(example = "1000000")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
