package com.re.rikkei_bank_manager.transaction.mapper;

import com.re.rikkei_bank_manager.account.entity.Account;
import com.re.rikkei_bank_manager.common.enums.TransactionType;
import com.re.rikkei_bank_manager.transaction.dto.response.StatementResponse;
import com.re.rikkei_bank_manager.transaction.dto.response.TransferResponse;
import com.re.rikkei_bank_manager.transaction.entity.BankTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransferResponse toTransferResponse(BankTransaction tx) {
        if (tx == null) return null;
        TransferResponse dto = new TransferResponse();
        org.springframework.beans.BeanUtils.copyProperties(tx, dto);
        if (tx.getFromAccount() != null) dto.setFromAccountNumber(tx.getFromAccount().getAccountNumber());
        if (tx.getToAccount() != null) dto.setToAccountNumber(tx.getToAccount().getAccountNumber());
        return dto;
    }

    public StatementResponse toStatementResponse(BankTransaction tx, Long accountId) {
        if (tx == null) return null;
        StatementResponse dto = new StatementResponse();
        org.springframework.beans.BeanUtils.copyProperties(tx, dto);
        
        boolean debit = java.util.Objects.equals(tx.getFromAccount().getId(), accountId);
        Account counterparty = debit ? tx.getToAccount() : tx.getFromAccount();
        dto.setType(debit ? TransactionType.DEBIT : TransactionType.CREDIT);
        dto.setCounterpartyAccountNumber(counterparty != null ? counterparty.getAccountNumber() : null);
        return dto;
    }
}
