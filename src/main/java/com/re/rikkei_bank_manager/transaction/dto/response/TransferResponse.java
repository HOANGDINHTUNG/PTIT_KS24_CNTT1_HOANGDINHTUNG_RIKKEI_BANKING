package com.re.rikkei_bank_manager.transaction.dto.response;

import com.re.rikkei_bank_manager.common.enums.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String transactionCode;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
