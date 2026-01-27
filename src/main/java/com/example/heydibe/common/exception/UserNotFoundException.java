package com.example.heydibe.common.exception;

import com.example.heydibe.common.error.ErrorCode;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
