package com.re.rikkei_bank_manager.transaction.service;

import com.re.rikkei_bank_manager.transaction.dto.request.TransferRequest;
import com.re.rikkei_bank_manager.transaction.dto.response.StatementResponse;
import com.re.rikkei_bank_manager.transaction.dto.response.TransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransferResponse transfer(TransferRequest req);
    Page<StatementResponse> getStatement(Long accountId, Pageable pageable);
}
