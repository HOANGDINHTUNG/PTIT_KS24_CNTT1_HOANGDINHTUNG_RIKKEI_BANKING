package com.re.rikkei_bank_manager.user.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.common.response.ApiResult;
import com.re.rikkei_bank_manager.user.dto.request.UserCreateRequest;
import com.re.rikkei_bank_manager.user.dto.request.UserStatusRequest;
import com.re.rikkei_bank_manager.user.dto.request.UserUpdateRequest;
import com.re.rikkei_bank_manager.user.dto.response.UserResponseDto;
import com.re.rikkei_bank_manager.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Admin Complete User Management API")
@lombok.extern.slf4j.Slf4j
public class AdminUserController {
    private final UserService userService;

    @Operation(summary = "Get Users List", description = "Fetch all users.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<ApiResult<Page<UserResponseDto>>> getUsers(Pageable pageable) {
        log.info("Quản trị viên gửi request truy xuất danh sách toàn bộ người dùng.");
        try {
            Page<UserResponseDto> response = userService.getUsers(pageable);
            log.info("Phản hồi danh sách toàn trang người dùng thành công.");
            return ResponseEntity.ok(ApiResult.success("Fetched users successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi admin lấy dữ liệu danh sách người dùng: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Get User Profile", description = "Fetch a user by ID.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<UserResponseDto>> getUser(@PathVariable Long id) {
        log.info("Quản trị viên gửi request truy xuất chi tiết người dùng ID: {}", id);
        try {
            UserResponseDto response = userService.getUser(id);
            log.info("Phản hồi thông tin cá nhân người dùng ID: {} thành công.", id);
            return ResponseEntity.ok(ApiResult.success("Fetched user successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi admin truy xuất thông tin người dùng: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Create User", description = "Manually provision a user account.")
    @ApiResponse(responseCode = "201", description = "Created successfully")
    @PostMapping
    public ResponseEntity<ApiResult<UserResponseDto>> create(@Valid @RequestBody UserCreateRequest req) {
        log.info("Quản trị viên tạo mới tài khoản thủ công với username: {}", req.getUsername());
        try {
            UserResponseDto response = userService.createUser(req);
            log.info("Cấp tài khoản mới ({}) thành công.", req.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("Created user successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi admin khởi tạo tài khoản mới: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Update User", description = "Update an existing user account.")
    @ApiResponse(responseCode = "200", description = "Updated successfully")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<UserResponseDto>> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        log.info("Quản trị viên gửi request chỉnh sửa thông tin người dùng ID: {}", id);
        try {
            UserResponseDto response = userService.updateUser(id, req);
            log.info("Cập nhật thông tin cho người dùng ID {} thành công.", id);
            return ResponseEntity.ok(ApiResult.success("Updated user successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi admin sửa thông tin người dùng: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Change Status", description = "Block or Unblock a user account.")
    @ApiResponse(responseCode = "200", description = "Status changed successfully")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResult<UserResponseDto>> status(@PathVariable Long id, @Valid @RequestBody UserStatusRequest req) {
        log.info("Quản trị viên đóng/mở tài khoản ID: {} (Kích hoạt: {})", id, req.getActive());
        try {
            UserResponseDto response = userService.changeStatus(id, req);
            log.info("Thay đổi trạng thái tài khoản ID {} thành công.", id);
            return ResponseEntity.ok(ApiResult.success("Changed user status successfully", response));
        } catch (Exception e) {
            log.error("Lỗi khi admin cập nhật trạng thái tài khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Delete User", description = "Soft delete a user account from the system.")
    @ApiResponse(responseCode = "204", description = "Deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Quản trị viên thực hiện thao tác xóa tài khoản ID: {}", id);
        try {
            userService.deleteUser(id);
            log.info("Xóa tài khoản ID {} thành công.", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Lỗi khi admin xóa tài khoản {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}

