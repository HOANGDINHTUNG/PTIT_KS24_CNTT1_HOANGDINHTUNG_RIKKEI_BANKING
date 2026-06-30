package com.re.rikkei_bank_manager.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
public class TransferRequest {
    @NotBlank private String fromAccountNumber;
    @NotBlank private String toAccountNumber;
    @NotNull @DecimalMin(value = "0.01", message = "must be greater than 0")
    private BigDecimal amount;
    private String description;
    @NotBlank 
    @Pattern(regexp = "^[0-9]{6}$", message = "must contain exactly 6 digits")
    private String transactionPin;
}
