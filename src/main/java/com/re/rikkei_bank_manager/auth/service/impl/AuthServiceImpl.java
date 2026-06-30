package com.re.rikkei_bank_manager.auth.service.impl;

import com.re.rikkei_bank_manager.auth.dto.request.ForgotPasswordRequest;
import com.re.rikkei_bank_manager.auth.dto.request.LoginRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank_manager.auth.dto.request.ResetPasswordRequest;
import com.re.rikkei_bank_manager.auth.dto.response.AuthResponse;
import com.re.rikkei_bank_manager.auth.dto.response.ForgotPasswordResponse;
import com.re.rikkei_bank_manager.auth.dto.request.RegisterRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RegisterKycRequest;
import com.re.rikkei_bank_manager.common.enums.RoleName;
import com.re.rikkei_bank_manager.role.entity.Role;
import com.re.rikkei_bank_manager.role.repository.RoleRepository;
import com.re.rikkei_bank_manager.auth.entity.*;
import com.re.rikkei_bank_manager.auth.mapper.AuthMapper;
import com.re.rikkei_bank_manager.auth.repository.*;
import com.re.rikkei_bank_manager.kyc.entity.KycProfile;
import com.re.rikkei_bank_manager.kyc.repository.KycProfileRepository;
import com.re.rikkei_bank_manager.common.enums.KycStatus;
import com.re.rikkei_bank_manager.kyc.service.StorageService;
import com.re.rikkei_bank_manager.common.exception.*;
import com.re.rikkei_bank_manager.security.JwtService;
import com.re.rikkei_bank_manager.user.entity.User;
import com.re.rikkei_bank_manager.user.repository.UserRepository;
import com.re.rikkei_bank_manager.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Service @RequiredArgsConstructor @Transactional(readOnly = true) @Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final StorageService storageService;
    private final KycProfileRepository kycProfileRepository;
    private final StringRedisTemplate redisTemplate;
    private final AuthMapper authMapper;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    @Override
    @Transactional
    public void register(RegisterRequest req) {
        log.info("Bắt đầu xử lý đăng ký tài khoản mới cho user: {}", req.getUsername());
        try {
            validateRegistration(req.getUsername(), req.getEmail());
            User user = createUserEntity(req.getUsername(), req.getPassword(), req.getPhoneNumber(), req.getEmail());
            userRepository.save(user);
            log.info("Đăng ký tài khoản thành công cho user: {}", req.getUsername());
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi đăng ký tài khoản cho user {}: {}", req.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void registerWithKyc(RegisterKycRequest req) {
        log.info("Bắt đầu xử lý đăng ký tài khoản kèm eKYC cho user: {}", req.getUsername());
        try {
            validateRegistration(req.getUsername(), req.getEmail());
            if (req.getFile() == null || req.getFile().isEmpty()) throw new FileUploadException("KYC file is required");
            if (req.getFile().getSize() > MAX_FILE_SIZE) throw new FileUploadException("KYC file must not exceed 5MB");
            String type = req.getFile().getContentType();
            if (type == null || !ALLOWED_TYPES.contains(type)) throw new FileUploadException("Only jpg, jpeg, png, or pdf files are allowed");
            
            String kycUrl = storageService.uploadKycFile(req.getFile());
            
            User user = createUserEntity(req.getUsername(), req.getPassword(), req.getPhoneNumber(), req.getEmail());
            userRepository.save(user);
            
            KycProfile profile = KycProfile.builder()
                    .user(user)
                    .fullName(req.getFullName())
                    .idNumber(req.getIdNumber())
                    .dob(req.getDob())
                    .sex(req.getSex())
                    .address(req.getAddress())
                    .idCardFrontUrl(kycUrl)
                    .status(KycStatus.PENDING)
                    .build();
            kycProfileRepository.save(profile);
            log.info("Đăng ký kèm eKYC thành công cho user: {}", req.getUsername());
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi đăng ký eKYC cho user {}: {}", req.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    private void validateRegistration(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
    }

    private User createUserEntity(String username, String password, String phoneNumber, String email) {
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: CUSTOMER"));
        return User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .email(email)
                .active(true)
                .kyc(false)
                .role(customerRole)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        log.info("Bắt đầu xử lý đăng nhập cho user: {}", req.getUsername());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            User user = userRepository.findByUsername(req.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("Invalid login information"));
            if (!user.isActive()) throw new ForbiddenException("User account is disabled");
            
            log.info("Đăng nhập thành công cho user: {}", req.getUsername());
            return issueTokens(user);
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi đăng nhập cho user {}: {}", req.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) {
        log.info("Bắt đầu xử lý cấp lại token mới.");
        try {
            RefreshToken stored = refreshTokenRepository.findByToken(req.getRefreshToken())
                    .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));
            if (stored.isRevoked()) throw new UnauthorizedException("Refresh token has been revoked");
            if (stored.getExpiryDate().isBefore(LocalDateTime.now())) throw new UnauthorizedException("Refresh token expired");
            stored.setRevoked(true);
            
            log.info("Cấp lại token thành công cho user: {}", stored.getUser().getUsername());
            return issueTokens(stored.getUser());
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi cấp lại token: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader) {
        log.info("Bắt đầu xử lý đăng xuất.");
        try {
            if (!org.springframework.util.StringUtils.hasText(authorizationHeader) || !org.springframework.util.StringUtils.startsWithIgnoreCase(authorizationHeader, "Bearer ")) {
                throw new UnauthorizedException("Bearer access token is required");
            }
            String accessToken = authorizationHeader.substring(7);
            String username = jwtService.extractUsername(accessToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
            
            try {
                if (Boolean.FALSE.equals(redisTemplate.hasKey("blacklist:" + accessToken))) {
                    long expirationMs = jwtService.extractExpirationAsLocalDateTime(accessToken).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();
                    if (expirationMs > 0) {
                        redisTemplate.opsForValue().set("blacklist:" + accessToken, "true", expirationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
                    }
                }
            } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
                log.warn("Redis is unreachable. Unable to add token to blacklist.");
            }
            
            refreshTokenRepository.revokeAllByUserId(user.getId());
            log.info("Đăng xuất thành công cho user: {}", username);
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi đăng xuất: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req) {
        log.info("Bắt đầu xử lý quên mật khẩu cho email: {}", req.getEmail());
        try {
            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Email not found"));
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);
            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .token(token).expiryDate(expiry).used(false).user(user).build());
            
            log.info("Đã tạo reset token thành công cho email: {}", req.getEmail());
            return ForgotPasswordResponse.builder()
                    .resetToken(token).expiryDate(expiry)
                    .note("Demo mode: use this resetToken in /api/auth/reset-password. Production should send it by email.")
                    .build();
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi tạo reset token cho email {}: {}", req.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        log.info("Bắt đầu xử lý đặt lại mật khẩu.");
        try {
            PasswordResetToken token = passwordResetTokenRepository.findByToken(req.getResetToken())
                    .orElseThrow(() -> new UnauthorizedException("Reset token not found"));
            if (token.isUsed()) throw new UnauthorizedException("Reset token already used");
            if (token.getExpiryDate().isBefore(LocalDateTime.now())) throw new UnauthorizedException("Reset token expired");
            token.getUser().setPassword(passwordEncoder.encode(req.getNewPassword()));
            token.setUsed(true);
            log.info("Đặt lại mật khẩu thành công cho user: {}", token.getUser().getUsername());
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi đặt lại mật khẩu: {}", e.getMessage(), e);
            throw e;
        }
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        LocalDateTime refreshExpiry = LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000);
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken).expiryDate(refreshExpiry).revoked(false).user(user).build());
        return authMapper.toResponse(user, accessToken, refreshToken, jwtService.getAccessTokenExpirationMs());
    }
}
