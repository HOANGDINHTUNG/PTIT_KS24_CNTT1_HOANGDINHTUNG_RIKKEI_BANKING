package com.re.rikkei_bank_manager.common.exception;

import com.re.rikkei_bank_manager.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({BadRequestException.class, FileUploadException.class})
    public ResponseEntity<ErrorResponse> bad(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler({DuplicateResourceException.class, InsufficientBalanceException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class, LockedException.class})
    public ResponseEntity<ErrorResponse> unauth(RuntimeException ex, HttpServletRequest req) {
        String message;
        if (ex instanceof BadCredentialsException) message = "Username or password is incorrect";
        else if (ex instanceof LockedException) message = "User account is locked";
        else message = ex.getMessage();
        return build(HttpStatus.UNAUTHORIZED, message, req);
    }

    @ExceptionHandler({ForbiddenException.class, DisabledException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> forbidden(RuntimeException ex, HttpServletRequest req) {
        String message = ex instanceof DisabledException ? "User account is disabled" : ex.getMessage();
        return build(HttpStatus.FORBIDDEN, message, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldError).collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraint(ConstraintViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unknown(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error: " + ex.getMessage(), req);
    }

    private String fieldError(FieldError e) { return e.getField() + " " + e.getDefaultMessage(); }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now()).status(status.value()).error(status.getReasonPhrase())
                .message(message).path(req.getRequestURI()).build());
    }
}
