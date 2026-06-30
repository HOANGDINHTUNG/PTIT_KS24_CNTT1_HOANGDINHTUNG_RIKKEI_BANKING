package com.re.rikkei_bank_manager.audit.repository;

import com.re.rikkei_bank_manager.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
