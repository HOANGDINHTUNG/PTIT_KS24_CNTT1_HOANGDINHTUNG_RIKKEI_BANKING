package com.re.rikkei_bank_manager.auth.service;

import com.re.rikkei_bank_manager.auth.dto.request.*;
import com.re.rikkei_bank_manager.auth.dto.response.AuthResponse;
import com.re.rikkei_bank_manager.auth.entity.*;
import com.re.rikkei_bank_manager.auth.mapper.AuthMapper;
import com.re.rikkei_bank_manager.auth.repository.*;
import com.re.rikkei_bank_manager.common.enums.RoleName;
import com.re.rikkei_bank_manager.common.exception.*;
import com.re.rikkei_bank_manager.role.entity.Role;
import com.re.rikkei_bank_manager.role.repository.RoleRepository;
import com.re.rikkei_bank_manager.security.JwtService;
import com.re.rikkei_bank_manager.user.entity.User;
import com.re.rikkei_bank_manager.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuthMapper authMapper;
    @Mock private StringRedisTemplate redisTemplate;

    @InjectMocks
    private com.re.rikkei_bank_manager.auth.service.impl.AuthServiceImpl authService;

    @Test
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user1"); req.setPassword("pass"); req.setEmail("a@b.c"); req.setPhoneNumber("123");
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.c")).thenReturn(false);
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(new Role()));
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");

        authService.register(req);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateUsername() {
        RegisterRequest req = new RegisterRequest(); 
        req.setUsername("user1"); 
        req.setEmail("a@b.c");
        
        when(userRepository.existsByUsername("user1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
    }

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest(); req.setUsername("user1"); req.setPassword("pass");
        User user = new User(); user.setId(1L); user.setUsername("user1"); user.setActive(true);
        Role role = new Role(); role.setName(RoleName.CUSTOMER); user.setRole(role);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh_token");
        when(authMapper.toResponse(eq(user), eq("access_token"), eq("refresh_token"), anyLong()))
            .thenReturn(AuthResponse.builder().accessToken("access_token").username("user1").build());

        var response = authService.login(req);

        assertEquals("access_token", response.getAccessToken());
        assertEquals("user1", response.getUsername());
    }

    @Test
    void testLogin_UserInactive() {
        LoginRequest req = new LoginRequest(); req.setUsername("user1"); req.setPassword("pass");
        User user = new User(); user.setUsername("user1"); user.setActive(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> authService.login(req));
    }

    @Test
    void testRefresh_TokenNotFound() {
        RefreshTokenRequest req = new RefreshTokenRequest(); req.setRefreshToken("invalid");
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refresh(req));
    }
}
