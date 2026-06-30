package com.re.rikkei_bank_manager.user.service;

import com.re.rikkei_bank_manager.user.dto.request.UserCreateRequest;
import com.re.rikkei_bank_manager.user.dto.request.UserStatusRequest;
import com.re.rikkei_bank_manager.user.dto.request.UserUpdateRequest;
import com.re.rikkei_bank_manager.user.dto.response.UserResponseDto;
import com.re.rikkei_bank_manager.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponseDto> getUsers(Pageable pageable);
    UserResponseDto getUser(Long id);
    UserResponseDto createUser(UserCreateRequest req);
    UserResponseDto updateUser(Long id, UserUpdateRequest req);
    UserResponseDto changeStatus(Long id, UserStatusRequest req);
    void deleteUser(Long id);
    User findUser(Long id);
}
