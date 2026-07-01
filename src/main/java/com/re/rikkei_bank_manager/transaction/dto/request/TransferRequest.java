package com.re.rikkei_bank_manager.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter @Setter
public class TransferRequest {
    @NotBlank
    @Schema(example = "0123456789")
    private String fromAccountNumber;

    @NotBlank
    @Schema(example = "9876543210")
    private String toAccountNumber;

    @NotNull
    @DecimalMin(value = "0.01", message = "must be greater than 0")
    @Schema(example = "50000")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(example = "Chuyen tien tra an trua")
    private String description;

    @NotBlank 
    @Pattern(regexp = "^[0-9]{6}$", message = "must contain exactly 6 digits")
    @Schema(example = "740945")
    private String transactionPin;
}
