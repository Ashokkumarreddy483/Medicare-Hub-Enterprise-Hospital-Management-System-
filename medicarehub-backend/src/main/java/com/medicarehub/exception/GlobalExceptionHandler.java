package com.medicarehub.exception;

import com.medicarehub.dto.MessageResponseDto;
import org.slf4j.Logger; // Using SLF4J for logging
import org.slf4j.LoggerFactory; // Using SLF4J for logging
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Handles @PreAuthorize failures if not caught more specifically
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(new MessageResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<MessageResponseDto> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        logger.warn("Resource already exists: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(new MessageResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<MessageResponseDto> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        logger.warn("User creation conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 is often better for "already exists"
                .body(new MessageResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<MessageResponseDto> handleRoleNotFoundException(RoleNotFoundException ex) {
        logger.warn("Role not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400, as it's likely a configuration or bad input issue
                .body(new MessageResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class) // For custom bad request scenarios from services
    public ResponseEntity<MessageResponseDto> handleBadRequestException(BadRequestException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(new MessageResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenAccessException.class) // Custom authorization failure
    public ResponseEntity<MessageResponseDto> handleForbiddenAccessException(ForbiddenAccessException ex) {
        logger.warn("Forbidden access: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(new MessageResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class) // From Spring Security's @PreAuthorize failures
    public ResponseEntity<MessageResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied by security rule: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(new MessageResponseDto("Access Denied: You do not have permission to perform this action."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // For @Valid DTO validation failures
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            if (errors.containsKey(error.getField())) {
                errors.put(error.getField(), errors.get(error.getField()) + ", " + error.getDefaultMessage());
            } else {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        });
        logger.warn("Validation errors: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(BadCredentialsException.class) // Specific to login failures
    public ResponseEntity<MessageResponseDto> handleBadCredentialsException(BadCredentialsException ex) {
        logger.warn("Authentication failed (Bad Credentials): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(new MessageResponseDto("Error: Invalid username or password."));
    }

    @ExceptionHandler(AuthenticationException.class) // Broader authentication issues
    public ResponseEntity<MessageResponseDto> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(new MessageResponseDto("Authentication error: " + ex.getMessage()));
    }

    // Generic fallback handler for any other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDto> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred: ", ex); // Log the full stack trace for unexpected errors
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(new MessageResponseDto("An unexpected internal server error occurred. Please try again later or contact support."));
    }
}