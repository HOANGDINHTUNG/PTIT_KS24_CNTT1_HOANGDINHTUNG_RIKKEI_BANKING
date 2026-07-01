package com.re.rikkei_bank_manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.rikkei_bank_manager.common.response.ErrorResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), ErrorResponse.builder()
                .timestamp(LocalDateTime.now()).status(401).error("Unauthorized")
                .message("Missing or invalid token").path(req.getRequestURI()).build());
    }
}
