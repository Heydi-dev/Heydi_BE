package com.example.heydibe.common.exception;

import com.example.heydibe.common.error.ErrorCode;

public class InvalidPasswordException extends CustomException {
    public InvalidPasswordException(ErrorCode errorCode) {
        super(errorCode);
    }
}
