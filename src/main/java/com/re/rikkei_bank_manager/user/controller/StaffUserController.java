package com.re.rikkei_bank_manager.user.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.common.response.ApiResult;
import com.re.rikkei_bank_manager.user.dto.response.UserResponseDto;
import com.re.rikkei_bank_manager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/users")
@RequiredArgsConstructor
@Tag(name = "Staff User Management", description = "Staff End-User Management API")
@lombok.extern.slf4j.Slf4j
public class StaffUserController {
    private final UserService userService;

    @Operation(summary = "Get Users List", description = "Get a list of customer accounts.")
    @ApiResponse(responseCode = "200", description = "Fetched list successfully")
    @GetMapping
    public ResponseEntity<ApiResult<Page<UserResponseDto>>> getUsers(Pageable pageable) {
        log.info("Nhân viên gửi request tải danh sách khách hàng cục bộ.");
        try {
            Page<UserResponseDto> response = userService.getUsers(pageable);
            log.info("Phản hồi thông tin danh sách khách hàng cho nhân viên thành công.");
            return ResponseEntity.ok(ApiResult.success("Fetched users successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi lấy dữ liệu danh sách khách hàng: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Get User Profile", description = "Fetch a user by their ID.")
    @ApiResponse(responseCode = "200", description = "Fetched profile successfully")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<UserResponseDto>> getUser(@PathVariable Long id) {
        log.info("Nhân viên gửi request tải thông tin khách hàng ID: {}", id);
        try {
            UserResponseDto response = userService.getUser(id);
            log.info("Phản hồi thông tin cá nhân khách hàng ID: {} thành công.", id);
            return ResponseEntity.ok(ApiResult.success("Fetched user successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi tải thông tin khách hàng: {}", e.getMessage(), e);
            throw e;
        }
    }
}

