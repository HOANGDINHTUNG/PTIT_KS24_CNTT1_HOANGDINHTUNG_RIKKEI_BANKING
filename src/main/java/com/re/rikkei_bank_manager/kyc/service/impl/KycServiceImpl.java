package com.re.rikkei_bank_manager.kyc.service.impl;

import com.re.rikkei_bank_manager.common.enums.KycStatus;
import com.re.rikkei_bank_manager.common.exception.*;
import com.re.rikkei_bank_manager.common.util.SecurityUtils;
import com.re.rikkei_bank_manager.kyc.dto.response.KycResponse;
import com.re.rikkei_bank_manager.kyc.dto.request.KycUploadRequest;
import com.re.rikkei_bank_manager.kyc.entity.KycProfile;
import com.re.rikkei_bank_manager.kyc.mapper.KycMapper;
import com.re.rikkei_bank_manager.kyc.repository.KycProfileRepository;
import com.re.rikkei_bank_manager.user.entity.User;
import com.re.rikkei_bank_manager.user.repository.UserRepository;
import com.re.rikkei_bank_manager.kyc.service.StorageService;
import com.re.rikkei_bank_manager.kyc.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Service @RequiredArgsConstructor @Transactional(readOnly = true) @Slf4j
public class KycServiceImpl implements KycService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final KycMapper kycMapper;

    @Override
    @Transactional
    public KycResponse upload(KycUploadRequest req) {
        log.info("Bắt đầu xử lý tải lên hồ sơ eKYC cho tài khoản sắp định danh.");
        try {
            User user = getCurrentUser();
            validateFile(req.getFile());
            String url = storageService.uploadKycFile(req.getFile());
            KycProfile profile = kycProfileRepository.findByUserId(user.getId()).orElseGet(() -> KycProfile.builder().user(user).build());
            profile.setFullName(req.getFullName());
            profile.setIdNumber(req.getIdNumber());
            profile.setDob(req.getDob());
            profile.setSex(req.getSex());
            profile.setAddress(req.getAddress());
            profile.setIdCardFrontUrl(url);
            profile.setStatus(KycStatus.PENDING);
            profile.setVerifiedAt(null);
            user.setKyc(false);
            log.info("Tải lên hồ sơ eKYC thành công cho tài khoản ID: {}", user.getId());
            return kycMapper.toResponse(kycProfileRepository.save(profile));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi xử lý tải lên eKYC: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<KycResponse> getPending(Pageable pageable) {
        log.info("Bắt đầu xử lý lấy danh sách eKYC đang chờ duyệt.");
        try {
            Page<KycResponse> responses = kycProfileRepository.findByStatus(KycStatus.PENDING, pageable).map(kycMapper::toResponse);
            log.info("Lấy danh sách chờ duyệt eKYC thành công.");
            return responses;
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi lấy danh sách eKYC chờ duyệt: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public KycResponse approve(Long id) {
        log.info("Bắt đầu xử lý phê duyệt hồ sơ eKYC ID: {}", id);
        try {
            KycProfile p = find(id);
            p.setStatus(KycStatus.CONFIRM);
            p.setVerifiedAt(LocalDateTime.now());
            p.getUser().setKyc(true);
            log.info("Phê duyệt hồ sơ eKYC thành công ID: {}", id);
            return kycMapper.toResponse(p);
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi phê duyệt hồ sơ eKYC ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public KycResponse reject(Long id) {
        log.info("Bắt đầu xử lý từ chối hồ sơ eKYC ID: {}", id);
        try {
            KycProfile p = find(id);
            p.setStatus(KycStatus.REJECT);
            p.setVerifiedAt(LocalDateTime.now());
            p.getUser().setKyc(false);
            log.info("Từ chối hồ sơ eKYC thành công ID: {}", id);
            return kycMapper.toResponse(p);
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi từ chối hồ sơ eKYC ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new FileUploadException("KYC file is required");
        if (file.getSize() > MAX_FILE_SIZE) throw new FileUploadException("KYC file must not exceed 5MB");
        String type = file.getContentType();
        if (type == null || !ALLOWED_TYPES.contains(type)) throw new FileUploadException("Only jpg, jpeg, png, or pdf files are allowed");
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new BadRequestException("Cannot identify current user");
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private KycProfile find(Long id) {
        return kycProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("KYC profile not found with id: " + id));
    }

}
