package com.example.banking.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<Map<String, Object>> handleBusiness(BusinessException exception) {
        return response(exception.getStatus().value(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Invalid request");
        return response(400, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException exception) {
        return response(409, "The username or account number is already in use");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException exception) {
        return response(401, "Invalid username or password");
    }

    private ResponseEntity<Map<String, Object>> response(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}

