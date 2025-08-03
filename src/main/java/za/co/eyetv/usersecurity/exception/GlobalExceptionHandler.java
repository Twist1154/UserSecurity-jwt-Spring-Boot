package za.co.eyetv.usersecurity.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import za.co.eyetv.usersecurity.dto.ErrorDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Rethabile Ntsekhe
 * @date: 09-04-2025
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ErrorDTO createErrorDTO(String message, HttpStatus status, WebRequest request) {
        log.error("Error occurred: {} - {}", status, message);
        return ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false))
                .build();
    }

    // Helper method for validation errors
    private ErrorDTO createValidationErrorDTO(MethodArgumentNotValidException e, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getDescription(false))
                .errors(errors)
                .build();
    }


    // Exception handlers
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDTO> handleBadCredentialsException(BadCredentialsException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorDTO("Invalid credentials", HttpStatus.UNAUTHORIZED, request));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleUsernameNotFoundException(UsernameNotFoundException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorDTO("User not found", HttpStatus.NOT_FOUND, request));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDTO> handleDisabledException(DisabledException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorDTO("Account is disabled", HttpStatus.FORBIDDEN, request));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorDTO> handleLockedException(LockedException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorDTO("Account is locked", HttpStatus.FORBIDDEN, request));
    }

    // JWT related exceptions
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDTO> handleExpiredJwtException(ExpiredJwtException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorDTO("JWT token has expired", HttpStatus.UNAUTHORIZED, request));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorDTO> handleMalformedJwtException(MalformedJwtException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorDTO("Invalid JWT token", HttpStatus.UNAUTHORIZED, request));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorDTO> handleSignatureException(SignatureException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorDTO("Invalid JWT signature", HttpStatus.UNAUTHORIZED, request));
    }

    // Access control exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDeniedException(AccessDeniedException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorDTO("Access denied", HttpStatus.FORBIDDEN, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationExceptions(MethodArgumentNotValidException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createValidationErrorDTO(e, request));
    }

    // Custom exceptions
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorDTO> handleRateLimitExceededException(RateLimitExceededException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(createErrorDTO(e.getMessage(), HttpStatus.TOO_MANY_REQUESTS, request));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleEmailAlreadyExistsException(EmailAlreadyExistsException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorDTO(e.getMessage(), HttpStatus.CONFLICT, request));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorDTO(e.getMessage(), HttpStatus.CONFLICT, request));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorDTO> handlePasswordMismatchException(PasswordMismatchException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorDTO(e.getMessage(), HttpStatus.UNAUTHORIZED, request));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorDTO> handleInvalidRefreshTokenException(InvalidRefreshTokenException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorDTO(e.getMessage(), HttpStatus.UNAUTHORIZED, request));
    }

    @ExceptionHandler(UserNotActivatedException.class)
    public ResponseEntity<ErrorDTO> handleUserNotActivatedException(UserNotActivatedException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorDTO(e.getMessage(), HttpStatus.FORBIDDEN, request));
    }

    // Generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleAllExceptions(Exception e, WebRequest request) {
        log.error("Unexpected error occurred: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorDTO("An unexpected error occurred. Please contact support.", HttpStatus.INTERNAL_SERVER_ERROR, request));
    }



/*    // Generic exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e, WebRequest request) {
        log.error("Unexpected error occurred", e);
        return createErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }*/
}