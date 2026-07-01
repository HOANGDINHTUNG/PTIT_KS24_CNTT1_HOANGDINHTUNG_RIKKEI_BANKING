package com.re.rikkei_bank_manager.kyc.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.common.response.ApiResult;
import com.re.rikkei_bank_manager.kyc.dto.response.KycResponse;
import com.re.rikkei_bank_manager.kyc.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/kyc")
@RequiredArgsConstructor
@Tag(name = "Customer KYC Management", description = "Customer KYC Upload API")
@Slf4j
public class CustomerKycController {
    private final KycService kycService;

    @Operation(summary = "Upload KYC Documents", description = "Upload identity info and ID card images.")
    @ApiResponse(responseCode = "200", description = "Uploaded successfully")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<KycResponse>> upload(@jakarta.validation.Valid @ModelAttribute com.re.rikkei_bank_manager.kyc.dto.request.KycUploadRequest req) {
        log.info("Khách hàng gửi request tải lên hồ sơ eKYC.");
        try {
            KycResponse response = kycService.upload(req);
            log.info("Phản hồi tải lên hồ sơ eKYC thành công cho khách hàng.");
            return ResponseEntity.ok(ApiResult.success("Uploaded KYC successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận yêu cầu tải lên hồ sơ eKYC từ khách hàng: {}", e.getMessage(), e);
            throw e;
        }
    }
}

