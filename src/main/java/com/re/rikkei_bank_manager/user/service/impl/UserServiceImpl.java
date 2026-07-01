package com.re.rikkei_bank_manager.user.service.impl;

import com.re.rikkei_bank_manager.common.exception.*;
import com.re.rikkei_bank_manager.role.entity.Role;
import com.re.rikkei_bank_manager.role.repository.RoleRepository;
import com.re.rikkei_bank_manager.user.dto.request.UserCreateRequest;
import com.re.rikkei_bank_manager.user.dto.request.UserStatusRequest;
import com.re.rikkei_bank_manager.user.dto.request.UserUpdateRequest;
import com.re.rikkei_bank_manager.user.dto.response.UserResponseDto;
import com.re.rikkei_bank_manager.user.entity.User;
import com.re.rikkei_bank_manager.user.repository.UserRepository;
import com.re.rikkei_bank_manager.common.util.SecurityUtils;
import com.re.rikkei_bank_manager.common.enums.RoleName;
import com.re.rikkei_bank_manager.user.mapper.UserMapper;
import com.re.rikkei_bank_manager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public Page<UserResponseDto> getUsers(Pageable pageable) {
        log.info("Bắt đầu xử lý lấy danh sách tải khoản người dùng.");
        try {
            User currentUser = SecurityUtils.getCurrentUserOrNull();
            Page<UserResponseDto> response;
            if (currentUser != null && currentUser.getRole().getName() == RoleName.STAFF) {
                response = userRepository.findByRoleNameProjected(RoleName.CUSTOMER, pageable);
            } else {
                response = userRepository.findAllProjected(pageable);
            }
            log.info("Lấy danh sách người dùng thành công.");
            return response;
        } catch (Exception e) {
            log.error("Lỗi trong quá trình lấy danh sách người dùng: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserResponseDto getUser(Long id) {
        log.info("Bắt đầu xử lý truy xuất thông tin chi tiết người dùng ID: {}", id);
        try {
            User user = findUser(id);
            User currentUser = SecurityUtils.getCurrentUserOrNull();
            if (currentUser != null && currentUser.getRole().getName() == RoleName.STAFF) {
                if (user.getRole().getName() != RoleName.CUSTOMER) {
                    throw new ForbiddenException("Staff can only view customer details");
                }
            }
            log.info("Truy xuất thông tin người dùng ID {} thành công.", id);
            return userMapper.toResponse(user);
        } catch (Exception e) {
            log.error("Lỗi khi truy xuất thông tin người dùng ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateRequest req) {
        log.info("Bắt đầu xử lý cấp mới tài khoản quản trị/nhân viên: {}", req.getUsername());
        try {
            if (userRepository.existsByUsername(req.getUsername())) throw new DuplicateResourceException("Username already exists");
            if (userRepository.existsByEmail(req.getEmail())) throw new DuplicateResourceException("Email already exists");
            Role role = roleRepository.findByName(req.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + req.getRoleName()));
            User user = User.builder()
                    .username(req.getUsername()).password(passwordEncoder.encode(req.getPassword()))
                    .phoneNumber(req.getPhoneNumber()).email(req.getEmail())
                    .active(true).kyc(false).role(role).build();
            User saved = userRepository.save(user);
            log.info("Cấp mới tài khoản {} thành công.", req.getUsername());
            return userMapper.toResponse(saved);
        } catch (Exception e) {
            log.error("Lỗi khi tạo mới tài khoản {}: {}", req.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequest req) {
        log.info("Bắt đầu xử lý cập nhật thông tin người dùng ID: {}", id);
        try {
            User user = findUser(id);
            User currentUser = SecurityUtils.getCurrentUserOrNull();
            if (currentUser != null && !java.util.Objects.equals(currentUser.getId(), id)) {
                if (user.getRole().getName() == RoleName.ADMIN) {
                    throw new ForbiddenException("Cannot modify another admin account");
                }
            }
            if (req.getEmail() != null && !java.util.Objects.equals(req.getEmail(), user.getEmail())) {
                if (userRepository.existsByEmail(req.getEmail())) throw new DuplicateResourceException("Email already exists");
                user.setEmail(req.getEmail());
            }
            if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
            if (req.getRoleName() != null) {
                Role role = roleRepository.findByName(req.getRoleName())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + req.getRoleName()));
                user.setRole(role);
            }
            log.info("Cập nhật thông tin người dùng ID {} thành công.", id);
            return userMapper.toResponse(user);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật tài khoản ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponseDto changeStatus(Long id, UserStatusRequest req) {
        log.info("Bắt đầu xử lý thay đổi trạng thái đăng nhập (kích hoạt: {}) của tài khoản ID: {}", req.getActive(), id);
        try {
            User currentUser = SecurityUtils.getCurrentUserOrNull();
            if (currentUser != null && java.util.Objects.equals(currentUser.getId(), id)) {
                throw new BadRequestException("You cannot change your own account status");
            }
            User user = findUser(id);
            if (user.getRole().getName() == RoleName.ADMIN) {
                throw new ForbiddenException("Cannot modify another admin account status");
            }
            user.setActive(req.getActive());
            log.info("Thay đổi trạng thái đăng nhập tài khoản ID {} thành công.", id);
            return userMapper.toResponse(user);
        } catch (Exception e) {
            log.error("Lỗi khi đổi trạng thái tài khoản ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Bắt đầu xử lý thu hồi (xóa mềm) tài khoản người dùng ID: {}", id);
        try {
            User currentUser = SecurityUtils.getCurrentUserOrNull();
            if (currentUser != null && java.util.Objects.equals(currentUser.getId(), id)) {
                throw new BadRequestException("You cannot delete your own account");
            }
            User user = findUser(id);
            if (user.getRole().getName() == RoleName.ADMIN) {
                throw new ForbiddenException("Cannot delete another admin account");
            }
            user.setActive(false);
            log.info("Thu hồi (xóa) tài khoản người dùng ID {} thành công.", id);
        } catch (Exception e) {
            log.error("Lỗi khi xóa tài khoản ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

}
