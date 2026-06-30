package com.re.rikkei_bank_manager.user.mapper;

import com.re.rikkei_bank_manager.user.dto.response.UserResponseDto;
import com.re.rikkei_bank_manager.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDto toResponse(User u) {
        if (u == null) return null;
        UserResponseDto dto = new UserResponseDto();
        org.springframework.beans.BeanUtils.copyProperties(u, dto);
        if (u.getRole() != null) {
            dto.setRoleName(u.getRole().getName());
        }
        return dto;
    }
}
