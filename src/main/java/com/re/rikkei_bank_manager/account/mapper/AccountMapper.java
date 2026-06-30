package com.re.rikkei_bank_manager.account.mapper;

import com.re.rikkei_bank_manager.account.dto.response.AccountResponseDto;
import com.re.rikkei_bank_manager.account.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponseDto toResponse(Account a) {
        if (a == null) return null;
        AccountResponseDto dto = new AccountResponseDto();
        org.springframework.beans.BeanUtils.copyProperties(a, dto);
        if (a.getUser() != null) {
            dto.setUserId(a.getUser().getId());
            dto.setUsername(a.getUser().getUsername());
        }
        return dto;
    }
}
