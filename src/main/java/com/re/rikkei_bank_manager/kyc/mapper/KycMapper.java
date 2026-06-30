package com.re.rikkei_bank_manager.kyc.mapper;

import com.re.rikkei_bank_manager.kyc.dto.response.KycResponse;
import com.re.rikkei_bank_manager.kyc.entity.KycProfile;
import com.re.rikkei_bank_manager.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class KycMapper {
    public KycResponse toResponse(KycProfile p) {
        if (p == null) return null;
        KycResponse dto = new KycResponse();
        org.springframework.beans.BeanUtils.copyProperties(p, dto);
        User u = p.getUser();
        if (u != null) {
            dto.setUserId(u.getId());
            dto.setUsername(u.getUsername());
        }
        return dto;
    }
}
