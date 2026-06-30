package com.re.rikkei_bank_manager.account.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.account.dto.request.AccountCreateRequest;
import com.re.rikkei_bank_manager.account.dto.request.AccountStatusRequest;
import com.re.rikkei_bank_manager.account.dto.response.AccountResponseDto;
import com.re.rikkei_bank_manager.account.service.AccountService;
import com.re.rikkei_bank_manager.common.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/accounts")
@RequiredArgsConstructor
@Tag(name = "Staff Account Management", description = "Staff Core Account Management API")
@lombok.extern.slf4j.Slf4j
public class StaffAccountController {
    private final AccountService accountService;

    @Operation(summary = "Get Accounts List", description = "Fetch all banking accounts.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<ApiResult<Page<AccountResponseDto>>> getAccounts(Pageable pageable) {
        log.info("Nhân viên gửi request tải danh sách toàn bộ tài khoản.");
        try {
            Page<AccountResponseDto> response = accountService.getAccounts(pageable);
            log.info("Phản hồi danh sách toàn bộ tài khoản thành công.");
            return ResponseEntity.ok(ApiResult.success("Fetched accounts successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi tiếp nhận request tải danh sách tài khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "View Account", description = "Fetch account details by ID.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<AccountResponseDto>> getAccount(@PathVariable Long id) {
        log.info("Nhân viên gửi request truy xuất thông tin tài khoản ID: {}", id);
        try {
            AccountResponseDto response = accountService.getAccount(id);
            log.info("Phản hồi thông tin tài khoản ID {} thành công.", id);
            return ResponseEntity.ok(ApiResult.success("Fetched account successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi truy xuất thông tin tài khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Create Account", description = "Provision a new banking account for a customer.")
    @ApiResponse(responseCode = "201", description = "Created successfully")
    @PostMapping
    public ResponseEntity<ApiResult<AccountResponseDto>> create(@Valid @RequestBody AccountCreateRequest req) {
        log.info("Nhân viên gửi request đăng ký tài khoản thanh toán mới cho khách hàng.");
        try {
            AccountResponseDto response = accountService.createAccount(req);
            log.info("Phản hồi đăng ký tài khoản thành công.");
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("Created account successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đăng ký tài khoản thanh toán: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Change Status", description = "Lock or unlock a specific account.")
    @ApiResponse(responseCode = "200", description = "Success")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResult<AccountResponseDto>> status(@PathVariable Long id, @Valid @RequestBody AccountStatusRequest req) {
        log.info("Nhân viên gửi request thay đổi trạng thái ({}). cho tài khoản ID: {}", req.getActive(), id);
        try {
            AccountResponseDto response = accountService.changeStatus(id, req);
            log.info("Phản hồi đổi trạng thái tài khoản thành công.");
            return ResponseEntity.ok(ApiResult.success("Changed account status successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận thay đổi trạng thái tài khoản: {}", e.getMessage(), e);
            throw e;
        }
    }
}

