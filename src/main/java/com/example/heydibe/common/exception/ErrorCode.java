package com.example.heydibe.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 인증 실패
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "Authentication failed."
    ),

    // 서버 내부 오류
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "Internal server error occurred."
    ),

    // 비밀번호 불일치
    PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "4001",
            "Password does not match."
    ),

    // 세션 오류
    SESSION_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "An error occurred while processing the session."
    ),

    // 로그인 실패
    LOGIN_FAILED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "Login failed."
    ),

    // 삭제된 사용자
    USER_DELETED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "User has been deleted."
    ),

    // 아이디 중복
    USERNAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "2011",
            "Username already exists."
    ),

    // 이미 삭제된 사용자
    ALREADY_DELETED_USER(
            HttpStatus.BAD_REQUEST,
            "4001",
            "User has already been deleted."
    ),

    // S3 Presigned URL 생성 실패
    S3_PRESIGNED_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "Failed to generate S3 Presigned URL."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
