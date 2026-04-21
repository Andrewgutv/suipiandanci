package com.fragmentwords.config;

import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException exception) {
        log.warn("Validation failed", exception);
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Invalid request parameters";
        return respond(HttpStatus.BAD_REQUEST, Result.badRequest(message));
    }

    @ExceptionHandler({
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<Result<Void>> handleBadRequest(Exception exception) {
        log.warn("Bad request", exception);
        String message = exception.getMessage() != null ? exception.getMessage() : "Invalid request parameters";
        return respond(HttpStatus.BAD_REQUEST, Result.badRequest(message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Void>> handleConflict(IllegalStateException exception) {
        log.warn("Conflict", exception);
        String message = exception.getMessage() != null ? exception.getMessage() : "Request conflict";
        return respond(HttpStatus.CONFLICT, Result.conflict(message));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(SecurityException exception) {
        log.warn("Unauthorized", exception);
        String message = exception.getMessage() != null ? exception.getMessage() : "Unauthorized";
        return respond(HttpStatus.UNAUTHORIZED, Result.unauthorized(message));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(ResourceNotFoundException exception) {
        log.warn("Not found", exception);
        String message = exception.getMessage() != null ? exception.getMessage() : "Resource not found";
        return respond(HttpStatus.NOT_FOUND, Result.notFound(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleInternalError(Exception exception) {
        log.error("Unhandled server error", exception);
        String message = exception.getMessage() != null ? exception.getMessage() : "Internal server error";
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, Result.error(message));
    }

    private ResponseEntity<Result<Void>> respond(HttpStatus status, Result<Void> body) {
        return ResponseEntity.status(status).body(body);
    }
}
