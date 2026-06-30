package com.re.rikkei_bank_manager.kyc.repository;

import com.re.rikkei_bank_manager.common.enums.KycStatus;
import com.re.rikkei_bank_manager.kyc.entity.KycProfile;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
    Optional<KycProfile> findByUserId(Long userId);
    Page<KycProfile> findByStatus(KycStatus status, Pageable pageable);
}
