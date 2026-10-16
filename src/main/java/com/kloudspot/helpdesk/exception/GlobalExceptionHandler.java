package com.kloudspot.helpdesk.exception;

import com.kloudspot.helpdesk.auth.model.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequest(BadRequestException ex, HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> forbidden(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Forbidden";
        return body(HttpStatus.FORBIDDEN, message, request, ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> unauthorized(AuthenticationException ex, HttpServletRequest request) {
        return body(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return body(HttpStatus.BAD_REQUEST, message, request, ex);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> database(DataAccessException ex, HttpServletRequest request) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> fallback(Exception ex, HttpServletRequest request) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request, ex);
    }

    private ResponseEntity<ErrorResponse> body(
            HttpStatus status, String message, HttpServletRequest request, Exception ex) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String kind = ex.getClass().getSimpleName();
        String user = currentUser();
        if (status.is5xxServerError()) {
            log.error("{} {} -> {} {} [{}] user={}", method, path, status.value(), message, kind, user, ex);
        } else {
            log.warn("{} {} -> {} {} [{}] user={}", method, path, status.value(), message, kind, user);
        }
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()));
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUser user) {
            return user.getEmail() + "/" + user.getRole();
        }
        return String.valueOf(authentication.getName());
    }
}
