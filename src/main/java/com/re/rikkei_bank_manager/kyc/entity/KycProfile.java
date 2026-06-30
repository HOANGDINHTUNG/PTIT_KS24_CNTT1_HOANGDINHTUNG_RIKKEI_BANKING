package com.re.rikkei_bank_manager.kyc.entity;

import com.re.rikkei_bank_manager.common.enums.KycStatus;
import com.re.rikkei_bank_manager.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "kyc_profiles")
public class KycProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_number", nullable = false, length = 50)
    private String idNumber;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(length = 20)
    private String sex;

    @Column(length = 255)
    private String address;

    @Column(name = "id_card_front_url", columnDefinition = "TEXT")
    private String idCardFrontUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus status;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = KycStatus.PENDING;
    }
}
