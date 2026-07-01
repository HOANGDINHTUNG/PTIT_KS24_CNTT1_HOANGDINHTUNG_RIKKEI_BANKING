package com.re.rikkei_bank_manager.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DepositResponse {
    private String transactionCode;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private LocalDateTime timestamp;
}
