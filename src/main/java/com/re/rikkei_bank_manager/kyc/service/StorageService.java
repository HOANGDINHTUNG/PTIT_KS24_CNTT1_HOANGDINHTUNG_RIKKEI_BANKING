package com.re.rikkei_bank_manager.kyc.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadKycFile(MultipartFile file);
}
