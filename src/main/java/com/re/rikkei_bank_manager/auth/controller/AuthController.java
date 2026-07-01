package com.re.rikkei_bank_manager.auth.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.re.rikkei_bank_manager.auth.dto.request.ForgotPasswordRequest;
import com.re.rikkei_bank_manager.auth.dto.request.LoginRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RegisterRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank_manager.auth.dto.request.ResetPasswordRequest;
import com.re.rikkei_bank_manager.auth.dto.response.AuthResponse;
import com.re.rikkei_bank_manager.auth.dto.response.ForgotPasswordResponse;
import com.re.rikkei_bank_manager.auth.service.AuthService;
import com.re.rikkei_bank_manager.common.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication, Registration, and JWT Version Control API")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register Account", description = "Create a new customer account without KYC.")
    @ApiResponse(responseCode = "201", description = "Registered successfully")
    @PostMapping("/register")
    public ResponseEntity<ApiResult<Void>> register(@Valid @RequestBody RegisterRequest req) {
        log.info("Gửi request đăng ký tài khoản mới lên hệ thống cho user: {}", req.getUsername());
        try {
            authService.register(req);
            log.info("Phản hồi đăng ký tài khoản thành công cho user: {}", req.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResult.success("Registered successfully", null));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận đăng ký tài khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Register with KYC", description = "Create a customer account and submit KYC documents.")
    @ApiResponse(responseCode = "201", description = "Registered with KYC successfully")
    @PostMapping(value = "/register-with-kyc", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<Void>> registerWithKyc(@Valid @ModelAttribute com.re.rikkei_bank_manager.auth.dto.request.RegisterKycRequest req) {
        log.info("Gửi request đăng ký tài khoản kèm eKYC cho user: {}", req.getUsername());
        try {
            authService.registerWithKyc(req);
            log.info("Phản hồi đăng ký eKYC thành công cho user: {}", req.getUsername());
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                    .body(ApiResult.success("Registered successfully with KYC", null));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận đăng ký eKYC: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Login", description = "Provide username and password to get tokens.")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @PostMapping("/login")
    public ResponseEntity<ApiResult<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        log.info("Gửi request đăng nhập hệ thống cho user: {}", req.getUsername());
        try {
            AuthResponse response = authService.login(req);
            log.info("Phản hồi đăng nhập thành công cho user: {}", req.getUsername());
            return ResponseEntity.ok(ApiResult.success("Login successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận đăng nhập: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Refresh Access Token", description = "Use Refresh Token to issue a new Access Token.")
    @ApiResponse(responseCode = "200", description = "Refresh successful")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        log.info("Gửi request cấp lại Access Token.");
        try {
            AuthResponse response = authService.refresh(req);
            log.info("Phản hồi cấp lại Access Token thành công.");
            return ResponseEntity.ok(ApiResult.success("Refreshed token successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình tiếp nhận cấp lại JWT Token: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Logout", description = "Revoke Refresh Token and blacklist Access Token.")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        log.info("Gửi request đăng xuất hệ thống.");
        try {
            authService.logout(authorizationHeader);
            log.info("Phản hồi đăng xuất thành công.");
            return ResponseEntity.ok(ApiResult.success("Logout successfully", null));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đăng xuất: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Forgot Password", description = "Send email to receive reset token.")
    @ApiResponse(responseCode = "200", description = "Reset token created successfully")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResult<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        log.info("Gửi request cấp lại mật khẩu cho email: {}", req.getEmail());
        try {
            ForgotPasswordResponse response = authService.forgotPassword(req);
            log.info("Phản hồi tạo Reset Token thành công cho email: {}", req.getEmail());
            return ResponseEntity.ok(ApiResult.success("Created reset token successfully", response));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình quên mật khẩu: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Reset Password", description = "Use reset token to set new password.")
    @ApiResponse(responseCode = "200", description = "Reset password successful")
    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResult<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        log.info("Gửi request thiết lập lại mật khẩu.");
        try {
            authService.resetPassword(req);
            log.info("Phản hồi đặt lại mật khẩu thành công.");
            return ResponseEntity.ok(ApiResult.success("Reset password successfully", null));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đổi mật khẩu: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Change Password", description = "User changes their own password.")
    @ApiResponse(responseCode = "200", description = "Changed successfully")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResult<Void>> changePassword(@Valid @RequestBody com.re.rikkei_bank_manager.auth.dto.request.ChangePasswordRequest req) {
        log.info("Gửi request đổi mật khẩu.");
        try {
            authService.changePassword(req);
            log.info("Phản hồi đổi mật khẩu thành công.");
            return ResponseEntity.ok(ApiResult.success("Changed password successfully", null));
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đổi mật khẩu: {}", e.getMessage(), e);
            throw e;
        }
    }
}

