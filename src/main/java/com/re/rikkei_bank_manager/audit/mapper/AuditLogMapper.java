package com.re.rikkei_bank_manager.audit.mapper;

import com.re.rikkei_bank_manager.audit.dto.response.AuditLogResponse;
import com.re.rikkei_bank_manager.audit.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    public AuditLogResponse toResponse(AuditLog log) {
        if (log == null) return null;
        AuditLogResponse dto = new AuditLogResponse();
        org.springframework.beans.BeanUtils.copyProperties(log, dto);
        return dto;
    }
}
