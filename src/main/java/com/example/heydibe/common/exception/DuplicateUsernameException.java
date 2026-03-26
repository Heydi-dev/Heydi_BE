package com.example.heydibe.common.exception;

import com.example.heydibe.common.error.ErrorCode;

public class DuplicateUsernameException extends CustomException {
    public DuplicateUsernameException(ErrorCode errorCode) {
        super(errorCode);
    }
}
