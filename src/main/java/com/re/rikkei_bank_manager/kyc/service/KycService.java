package com.re.rikkei_bank_manager.kyc.service;

import com.re.rikkei_bank_manager.kyc.dto.response.KycResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.re.rikkei_bank_manager.kyc.dto.request.KycUploadRequest;

public interface KycService {
    KycResponse upload(KycUploadRequest req);
    Page<KycResponse> getPending(Pageable pageable);
    KycResponse approve(Long id);
    KycResponse reject(Long id);
}
