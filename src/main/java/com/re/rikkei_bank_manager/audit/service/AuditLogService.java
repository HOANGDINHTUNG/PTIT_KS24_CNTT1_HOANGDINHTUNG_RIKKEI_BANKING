package com.re.rikkei_bank_manager.audit.service;

import com.re.rikkei_bank_manager.audit.entity.AuditLog;

public interface AuditLogService {
    void save(AuditLog log);
}
