package com.example.heydibe.common.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // ✅ log 필드 생성
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        // 비즈니스 예외는 그대로 코드/메시지 내려줌
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception e) {
        // ✅ 이제 여기서 진짜 원인이 콘솔에 찍힘
        log.error("UNHANDLED_EXCEPTION", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(9999, "서버 내부 오류"));
    }
}
