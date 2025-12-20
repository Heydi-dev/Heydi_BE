package com.example.heydibe.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 인증 실패
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "인증에 실패했습니다."
    ),

    // 서버 내부 오류
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "서버 내부 오류가 발생했습니다."
    ),

    // 비밀번호 불일치
    PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "4001",
            "비밀번호가 일치하지 않습니다."
    ),

    // 세션 오류
    SESSION_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "세션 처리 중 오류가 발생했습니다."
    ),

    // 로그인 실패
    LOGIN_FAILED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "로그인에 실패했습니다."
    ),

    // 삭제된 사용자
    USER_DELETED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "삭제된 사용자입니다."
    ),

    // 아이디 중복
    USERNAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "2011",
            "이미 존재하는 아이디입니다."
    ),

    // 이미 삭제된 사용자
    ALREADY_DELETED_USER(
            HttpStatus.BAD_REQUEST,
            "4001",
            "이미 삭제된 사용자입니다."
    ),

    // S3 Presigned URL 생성 실패
    S3_PRESIGNED_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "S3 Presigned URL 생성에 실패했습니다."
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
