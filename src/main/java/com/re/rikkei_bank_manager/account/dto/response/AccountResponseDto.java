package com.re.rikkei_bank_manager.account.dto.response;

import com.re.rikkei_bank_manager.common.enums.Currency;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private boolean active;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
