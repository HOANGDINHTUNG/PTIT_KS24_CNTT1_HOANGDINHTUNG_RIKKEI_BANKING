package com.re.rikkei_bank_manager.account.dto.response;

import com.re.rikkei_bank_manager.common.enums.Currency;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BalanceResponse {
    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
}
