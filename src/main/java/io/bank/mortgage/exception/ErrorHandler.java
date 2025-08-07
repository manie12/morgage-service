package io.bank.mortgage.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
  @ExceptionHandler(ValidationException.class)
  ResponseEntity<ErrorResponse> handle(ValidationException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest().body(ErrorResponse.of("VALIDATION_ERROR", ex.getMessage(), req.getRequestURI()));
  }
  // 403, 404, 409, 429, 500 similarly...
}