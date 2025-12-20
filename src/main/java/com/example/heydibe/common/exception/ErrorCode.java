package com.example.heydibe.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ?¸ì¦ ?¤íŒ¨
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "?¸ì¦???¤íŒ¨?ˆìŠµ?ˆë‹¤."
    ),

    // ?œë²„ ?´ë? ?¤ë¥˜
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "?œë²„ ?´ë? ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤."
    ),

    // ë¹„ë?ë²ˆí˜¸ ë¶ˆì¼ì¹?
    PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "4001",
            "ë¹„ë?ë²ˆí˜¸ê°€ ?¼ì¹˜?˜ì? ?ŠìŠµ?ˆë‹¤."
    ),

    // ?¸ì…˜ ?¤ë¥˜
    SESSION_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "?¸ì…˜ ì²˜ë¦¬ ì¤??¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤."
    ),

    // ë¡œê·¸???¤íŒ¨
    LOGIN_FAILED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "ë¡œê·¸?¸ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤."
    ),

    // ?? œ???¬ìš©??
    USER_DELETED(
            HttpStatus.UNAUTHORIZED,
            "401",
            "?? œ???¬ìš©?ì…?ˆë‹¤."
    ),

    // ?„ì´??ì¤‘ë³µ
    USERNAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "2011",
            "?´ë? ì¡´ì¬?˜ëŠ” ?„ì´?”ì…?ˆë‹¤."
    ),

    // ?´ë? ?? œ???¬ìš©??
    ALREADY_DELETED_USER(
            HttpStatus.BAD_REQUEST,
            "4001",
            "?´ë? ?? œ???¬ìš©?ì…?ˆë‹¤."
    ),

    // S3 Presigned URL ?ì„± ?¤íŒ¨
    S3_PRESIGNED_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            "S3 Presigned URL ?ì„±???¤íŒ¨?ˆìŠµ?ˆë‹¤."
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

