package com.schd.scheduler.config;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing
            ));

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
    }
}