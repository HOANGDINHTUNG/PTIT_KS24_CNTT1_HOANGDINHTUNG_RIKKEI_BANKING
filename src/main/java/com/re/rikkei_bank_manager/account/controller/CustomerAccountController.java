package com.re.rikkei_bank_manager.account.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.account.dto.request.PinChangeRequest;
import com.re.rikkei_bank_manager.account.dto.response.AccountResponseDto;
import com.re.rikkei_bank_manager.account.dto.response.BalanceResponse;
import com.re.rikkei_bank_manager.account.service.AccountService;
import com.re.rikkei_bank_manager.common.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/accounts")
@RequiredArgsConstructor
@Tag(name = "Customer Account", description = "Customer Personal Account API")
@lombok.extern.slf4j.Slf4j
public class CustomerAccountController {
    private final AccountService accountService;

    @Operation(summary = "Get My Accounts", description = "Fetch banking accounts owned by the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<ApiResult<List<AccountResponseDto>>> getMyAccounts() {
        log.info("Khách hàng gửi request truy xuất danh sách tài khoản cá nhân.");
        try {
            List<AccountResponseDto> response = accountService.getCurrentCustomerAccounts();
            log.info("Phản hồi danh sách tài khoản cá nhân thành công.");
            return ResponseEntity.ok(ApiResult.success("Fetched accounts successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi tiếp nhận request truy xuất tài khoản cá nhân: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Check Balance", description = "Retrieve the available balance.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<ApiResult<BalanceResponse>> getBalance(@PathVariable Long accountId) {
        log.info("Khách hàng gửi request vấn tin số dư cho tài khoản ID: {}", accountId);
        try {
            BalanceResponse response = accountService.getBalance(accountId);
            log.info("Phản hồi thông tin số dư thành công.");
            return ResponseEntity.ok(ApiResult.success("Fetched balance successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi vấn tin số dư tài khoản ID {}: {}", accountId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Change PIN Code", description = "Change the transaction PIN code.")
    @ApiResponse(responseCode = "200", description = "Success")
    @PatchMapping("/{accountId}/pin")
    public ResponseEntity<ApiResult<AccountResponseDto>> changePin(@PathVariable Long accountId, @Valid @RequestBody PinChangeRequest req) {
        log.info("Khách hàng gửi request thay đổi mã PIN giao dịch cho tài khoản ID: {}", accountId);
        try {
            AccountResponseDto response = accountService.changePin(accountId, req);
            log.info("Phản hồi thay đổi mã PIN thành công.");
            return ResponseEntity.ok(ApiResult.success("Changed transaction PIN successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đổi mã PIN: {}", e.getMessage(), e);
            throw e;
        }
    }
}

