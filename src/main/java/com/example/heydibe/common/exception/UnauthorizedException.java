package com.example.heydibe.common.exception;

import com.example.heydibe.common.error.ErrorCode;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
