package com.example.heydibe.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 BAD_REQUEST
    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "400",
            "잘못된 요청입니다"
    ),

    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "400",
            "요청 파라미터가 올바르지 않습니다"
    ),

    REQUIRED_FIELD_MISSING(
            HttpStatus.BAD_REQUEST,
            "400",
            "필수 입력값이 누락되었습니다"
    ),

    PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "400",
            "비밀번호가 일치하지 않습니다"
    ),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "로그인이 필요합니다"
    ),

    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "인증 정보가 없습니다"
    ),

    AUTHENTICATION_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "인증이 만료되었습니다"
    ),

    LOGIN_FAILED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "로그인에 실패했습니다"
    ),

    USER_DELETED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "삭제된 사용자입니다"
    ),

    // 403 FORBIDDEN
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "403",
            "권한이 없습니다"
    ),

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "403",
            "해당 리소스에 접근할 수 없습니다"
    ),

    // 404 NOT_FOUND
    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "404",
            "리소스를 찾을 수 없습니다"
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "404",
            "존재하지 않는 사용자입니다"
    ),

    // 409 CONFLICT
    USERNAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "409",
            "이미 사용 중인 아이디입니다"
    ),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "서버 오류가 발생했습니다"
    ),

    SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "일시적인 서버 오류입니다"
    ),

    SESSION_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "세션 처리 중 오류가 발생했습니다"
    ),

    S3_UPLOAD_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "파일 업로드에 실패했습니다"
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
