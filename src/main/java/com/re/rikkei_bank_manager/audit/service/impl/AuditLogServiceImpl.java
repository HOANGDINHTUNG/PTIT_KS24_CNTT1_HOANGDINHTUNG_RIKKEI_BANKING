package com.re.rikkei_bank_manager.audit.service.impl;

import com.re.rikkei_bank_manager.audit.entity.AuditLog;
import com.re.rikkei_bank_manager.audit.repository.AuditLogRepository;
import com.re.rikkei_bank_manager.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service @RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditLog logEvent) {
        try {
            auditLogRepository.save(logEvent);
        } catch (Exception e) {
            log.error("Lỗi khi lữu trữ AuditLog vào database: {}", e.getMessage(), e);
            throw e;
        }
    }
}
