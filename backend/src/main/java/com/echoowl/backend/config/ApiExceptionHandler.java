package com.echoowl.backend.config;

import com.echoowl.backend.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ApiError(ex.getReason()));
    }

    @ExceptionHandler({IllegalArgumentException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiError> badRequest(Exception ex) {
        log.warn("Bad request", ex);
        return ResponseEntity.badRequest().body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> generic(Exception ex) {
        log.error("Unhandled API error", ex);
        return ResponseEntity.internalServerError().body(new ApiError("Internal server error"));
    }
}
