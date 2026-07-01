package com.re.rikkei_bank_manager.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class StaffDepositRequest {
    @NotBlank(message = "Account number is required")
    @Schema(example = "0123456789")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000000.00", message = "Nhân viên chỉ hỗ trợ nạp số tiền từ 10,000,000 VND trở lên.")
    @Schema(example = "150000000")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(example = "Nạp tiền tiết kiệm tại quầy")
    private String description;
}
