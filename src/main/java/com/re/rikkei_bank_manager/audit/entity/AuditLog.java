package com.re.rikkei_bank_manager.audit.entity;

import com.re.rikkei_bank_manager.common.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 80) private String username;
    @Column(nullable = false, length = 80) private String action;
    @Column(name = "from_account_number", length = 30) private String fromAccountNumber;
    @Column(name = "to_account_number", length = 30) private String toAccountNumber;
    @Column(precision = 19, scale = 2) private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 500) private String message;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "execution_time_ms") private Long executionTimeMs;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
