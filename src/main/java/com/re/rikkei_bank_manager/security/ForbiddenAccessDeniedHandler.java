package com.re.rikkei_bank_manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.rikkei_bank_manager.common.response.ErrorResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component @RequiredArgsConstructor
public class ForbiddenAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
            throws IOException, ServletException {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), ErrorResponse.builder()
                .timestamp(LocalDateTime.now()).status(403).error("Forbidden")
                .message("You do not have permission to access this resource")
                .path(req.getRequestURI()).build());
    }
}
