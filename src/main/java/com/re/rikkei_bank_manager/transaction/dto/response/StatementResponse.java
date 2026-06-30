package com.re.rikkei_bank_manager.transaction.dto.response;

import com.re.rikkei_bank_manager.common.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StatementResponse {
    private String transactionCode;
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private String counterpartyAccountNumber;
}
