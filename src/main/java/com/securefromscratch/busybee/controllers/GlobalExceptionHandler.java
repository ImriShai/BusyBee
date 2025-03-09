package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.exceptions.*;
import org.owasp.safetypes.exception.TypeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle specific exceptions
    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchFileException(NoSuchFileException ex, WebRequest request) {
        LOGGER.warn("File not found: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "File not found");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex, WebRequest request) {
        LOGGER.error("Security error: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Security error");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TypeValidationException.class)
    public ResponseEntity<Map<String, String>> handleTypeValidationException(TypeValidationException ex, WebRequest request) {
        LOGGER.warn("Invalid file type: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid file type");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        LOGGER.error("Invalid argument: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid argument");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle custom exceptions
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, String>> handleTooManyRequestsException(TooManyRequestsException ex, WebRequest request) {
        LOGGER.warn("Too many requests: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Too many requests");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<Map<String, String>> handlePayloadTooLargeException(PayloadTooLargeException ex, WebRequest request) {
        LOGGER.warn("Payload too large: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Payload too large");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex, WebRequest request) {
        LOGGER.warn("Bad request: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Bad request");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(ConflictException ex, WebRequest request) {
        LOGGER.warn("Conflict: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotEnoughSpaceException.class)
    public ResponseEntity<Map<String, String>> handleNotEnoughSpaceException(NotEnoughSpaceException ex, WebRequest request) {
        LOGGER.error("Not enough space: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Not enough space");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INSUFFICIENT_STORAGE);
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex, WebRequest request) {
        if (ex.getCause() instanceof TypeValidationException) {
            LOGGER.warn("Invalid file type: {}", ex.getCause().getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid file type");
            response.put("message", ex.getCause().getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        LOGGER.error("An error occurred: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "An error occurred");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}