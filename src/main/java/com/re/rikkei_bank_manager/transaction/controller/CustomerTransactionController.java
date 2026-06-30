package com.re.rikkei_bank_manager.transaction.controller;

import com.re.rikkei_bank_manager.common.response.ApiResult;
import com.re.rikkei_bank_manager.transaction.dto.request.TransferRequest;
import com.re.rikkei_bank_manager.transaction.dto.response.StatementResponse;
import com.re.rikkei_bank_manager.transaction.dto.response.TransferResponse;
import com.re.rikkei_bank_manager.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Tag(name = "Customer Transaction", description = "Transaction Processing API")
@Slf4j
public class CustomerTransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Internal Transfer", description = "Execute a fund transfer between two accounts.")
    @ApiResponse(responseCode = "200", description = "Transfer successful")
    @PostMapping("/transactions/transfer")
    public ResponseEntity<ApiResult<TransferResponse>> transfer(@Valid @RequestBody TransferRequest req) {
        log.info("Khách hàng gửi request thực hiện chuyển khoản nội bộ.");
        try {
            TransferResponse response = transactionService.transfer(req);
            log.info("Phản hồi chuyển khoản thành công cho khách hàng.");
            return ResponseEntity.ok(ApiResult.success("Transferred money successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận yêu cầu chuyển khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "View Statement", description = "Fetch the transaction history of a given account.")
    @ApiResponse(responseCode = "200", description = "Statement fetched successfully")
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<ApiResult<Page<StatementResponse>>> statement(@PathVariable Long accountId, Pageable pageable) {
        log.info("Khách hàng gửi request kiểm tra sao kê giao dịch cho tài khoản ID: {}", accountId);
        try {
            Page<StatementResponse> response = transactionService.getStatement(accountId, pageable);
            log.info("Phản hồi sao kê giao dịch thành công cho khách hàng.");
            return ResponseEntity.ok(ApiResult.success("Fetched statement successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận yêu cầu xuất sao kê: {}", e.getMessage(), e);
            throw e;
        }
    }
}


