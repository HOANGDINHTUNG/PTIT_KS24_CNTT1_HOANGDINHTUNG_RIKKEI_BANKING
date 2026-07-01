package com.re.rikkei_bank_manager.audit.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.audit.dto.response.AuditLogResponse;
import com.re.rikkei_bank_manager.audit.mapper.AuditLogMapper;
import com.re.rikkei_bank_manager.audit.repository.AuditLogRepository;
import com.re.rikkei_bank_manager.common.response.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin Audit Logs", description = "Admin Audit Logs Management API")
@Slf4j
public class AdminAuditController {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Operation(summary = "View System Logs", description = "Get a paginated list of audit logs.")
    @ApiResponse(responseCode = "200", description = "Fetched list successfully")
    @GetMapping
    public ResponseEntity<ApiResult<Page<AuditLogResponse>>> getAuditLogs(Pageable pageable) {
        log.info("Quản trị viên gửi request truy xuất nhật ký hệ thống (Audit Logs).");
        try {
            Page<AuditLogResponse> response = auditLogRepository.findAll(pageable).map(auditLogMapper::toResponse);
            log.info("Phản hồi danh sách nhật ký hệ thống thành công.");
            return ResponseEntity.ok(ApiResult.success("Fetched audit logs successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi truy xuất nhật ký hệ thống: {}", e.getMessage(), e);
            throw e;
        }
    }
}

