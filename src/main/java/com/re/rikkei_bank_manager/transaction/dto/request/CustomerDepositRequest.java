package com.re.rikkei_bank_manager.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class CustomerDepositRequest {
    @NotBlank(message = "Account number is required")
    @Schema(example = "0123456789")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "100000000.00", message = "Số tiền nạp tối đa mỗi lần là 100,000,000 VND. Nạp lớn hơn vui lòng nhờ Nhân viên hỗ trợ.")
    @Schema(example = "5000000")
    private BigDecimal amount;

    @NotBlank 
    @Pattern(regexp = "^[0-9]{6}$", message = "must contain exactly 6 digits")
    @Schema(example = "740945")
    private String transactionPin;
}
