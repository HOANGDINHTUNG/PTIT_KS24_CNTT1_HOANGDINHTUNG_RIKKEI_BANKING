package com.re.rikkei_bank_manager.kyc.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.common.response.ApiResult;
import com.re.rikkei_bank_manager.kyc.dto.response.KycResponse;
import com.re.rikkei_bank_manager.kyc.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/kyc")
@RequiredArgsConstructor
@Tag(name = "Staff KYC Management", description = "Staff Verification and KYC Management API")
@Slf4j
public class StaffKycController {
    private final KycService kycService;

    @Operation(summary = "Get Pending KYC", description = "Fetch all KYC profiles currently in PENDING state.")
    @ApiResponse(responseCode = "200", description = "Fetched list successfully")
    @GetMapping("/pending")
    public ResponseEntity<ApiResult<Page<KycResponse>>> pending(Pageable pageable) {
        log.info("Nhân viên gửi request lấy danh sách hồ sơ eKYC đang chờ duyệt.");
        try {
            Page<KycResponse> response = kycService.getPending(pageable);
            log.info("Phản hồi danh sách eKYC chờ duyệt cho nhân viên thành công.");
            return ResponseEntity.ok(ApiResult.success("Fetched pending KYC profiles successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận yêu cầu lấy danh sách eKYC từ nhân viên: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Approve KYC", description = "Change profile state to CONFIRMED.")
    @ApiResponse(responseCode = "200", description = "Approved successfully")
    @PatchMapping("/{kycId}/approve")
    public ResponseEntity<ApiResult<KycResponse>> approve(@PathVariable Long kycId) {
        log.info("Nhân viên gửi request phê duyệt hồ sơ eKYC ID: {}", kycId);
        try {
            KycResponse response = kycService.approve(kycId);
            log.info("Phản hồi phê duyệt hồ sơ eKYC ID: {} thành công.", kycId);
            return ResponseEntity.ok(ApiResult.success("Approved KYC successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận yêu cầu phê duyệt eKYC: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Reject KYC", description = "Change profile state to REJECTED.")
    @ApiResponse(responseCode = "200", description = "Rejected successfully")
    @PatchMapping("/{kycId}/reject")
    public ResponseEntity<ApiResult<KycResponse>> reject(@PathVariable Long kycId) {
        log.info("Nhân viên gửi request từ chối hồ sơ eKYC ID: {}", kycId);
        try {
            KycResponse response = kycService.reject(kycId);
            log.info("Phản hồi từ chối hồ sơ eKYC ID: {} thành công.", kycId);
            return ResponseEntity.ok(ApiResult.success("Rejected KYC successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận yêu cầu từ chối eKYC: {}", e.getMessage(), e);
            throw e;
        }
    }
}

