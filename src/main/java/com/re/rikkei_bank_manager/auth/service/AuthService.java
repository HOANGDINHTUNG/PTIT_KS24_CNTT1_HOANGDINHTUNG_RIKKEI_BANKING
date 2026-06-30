package com.re.rikkei_bank_manager.auth.service;

import com.re.rikkei_bank_manager.auth.dto.request.ForgotPasswordRequest;
import com.re.rikkei_bank_manager.auth.dto.request.LoginRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RegisterRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RegisterKycRequest;
import com.re.rikkei_bank_manager.auth.dto.request.ResetPasswordRequest;
import com.re.rikkei_bank_manager.auth.dto.response.AuthResponse;
import com.re.rikkei_bank_manager.auth.dto.response.ForgotPasswordResponse;
public interface AuthService {
    void register(RegisterRequest req);
    void registerWithKyc(RegisterKycRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse refresh(RefreshTokenRequest req);
    void logout(String authorizationHeader);
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req);
    void resetPassword(ResetPasswordRequest req);
}
