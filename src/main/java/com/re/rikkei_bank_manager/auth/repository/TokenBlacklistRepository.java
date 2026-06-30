package com.re.rikkei_bank_manager.auth.repository;

import com.re.rikkei_bank_manager.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByAccessTokenAndExpiryDateAfter(String accessToken, LocalDateTime now);
    boolean existsByAccessToken(String accessToken);
}
