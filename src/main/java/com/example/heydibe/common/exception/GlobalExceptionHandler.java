package com.example.heydibe.common.exception;

import com.example.heydibe.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ErrorCode ec = ErrorCode.PASSWORD_MISMATCH; // ??기존???�는 코드 ?�용
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.fail(ec.getCode(), msg));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode ec = e.getErrorCode();
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e) {
        ErrorCode ec = ErrorCode.INTERNAL_SERVER_ERROR; // ???�전??기본�?
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }
}

