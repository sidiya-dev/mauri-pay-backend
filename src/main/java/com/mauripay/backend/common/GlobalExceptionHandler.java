package com.mauripay.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record ErrorResponse(
            OffsetDateTime timestamp,
            int status,
            String error,
            String code,
            String message,
            String path,
            Map<String, String> errors) {

        public ErrorResponse(HttpStatus status, String code, String message, String path,
                             Map<String, String> errors) {
            this(OffsetDateTime.now(Clock.systemUTC()), status.value(), status.getReasonPhrase(),
                    code, message, path, errors);
        }
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {
        log.warn("{} on {}: {}", ex.getCode(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(ex.getStatus(), ex.getCode(), ex.getMessage(),
                        request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fe ->
                        fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(), (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED,
                        "Validation failed", request.getRequestURI(), fields));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNEXPECTED_ERROR,
                        "An unexpected error occurred", request.getRequestURI(), Map.of()));
    }
}
