package com.re.rikkei_bank_manager.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.rikkei_bank_manager.auth.dto.request.LoginRequest;
import com.re.rikkei_bank_manager.auth.dto.request.RegisterRequest;
import com.re.rikkei_bank_manager.auth.dto.response.AuthResponse;
import com.re.rikkei_bank_manager.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    
    // Khai báo MockitoBean cho các security components tránh load context lỗi
    @MockitoBean private com.re.rikkei_bank_manager.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private com.re.rikkei_bank_manager.security.CustomUserDetailsService customUserDetailsService;
    @MockitoBean private com.re.rikkei_bank_manager.security.UnauthorizedEntryPoint unauthorizedEntryPoint;
    @MockitoBean private com.re.rikkei_bank_manager.security.ForbiddenAccessDeniedHandler forbiddenAccessDeniedHandler;
    @MockitoBean private com.re.rikkei_bank_manager.security.JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user1"); req.setPassword("pass"); req.setEmail("test@test.com"); req.setPhoneNumber("123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        RegisterRequest req = new RegisterRequest(); 
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("user1"); req.setPassword("pass");

        AuthResponse res = AuthResponse.builder().accessToken("token").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        LoginRequest req = new LoginRequest(); 
        req.setUsername("user1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk());
    }
}
