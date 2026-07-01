package com.re.rikkei_bank_manager.transaction.controller;

import com.re.rikkei_bank_manager.common.response.ApiResult;
import com.re.rikkei_bank_manager.transaction.dto.request.StaffDepositRequest;
import com.re.rikkei_bank_manager.transaction.dto.response.DepositResponse;
import com.re.rikkei_bank_manager.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Transaction", description = "Staff Transaction Management API")
@Slf4j
public class StaffTransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Deposit Money (Staff)", description = "Staff deposits large amounts directly into a customer account (Min 10M VND)")
    @ApiResponse(responseCode = "200", description = "Deposit successful")
    @PostMapping("/transactions/deposit")
    public ResponseEntity<ApiResult<DepositResponse>> staffDeposit(@Valid @RequestBody StaffDepositRequest req) {
        log.info("Nhân viên gửi request nạp tiền cho khách hàng.");
        try {
            DepositResponse response = transactionService.staffDeposit(req);
            log.info("Phản hồi nạp tiền thành công từ nhân viên.");
            return ResponseEntity.ok(ApiResult.success("Staff deposited money successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình nhân viên nạp tiền: {}", e.getMessage(), e);
            throw e;
        }
    }
}
