package com.example.heydibe.common.exception;

import com.example.heydibe.common.error.ErrorCode;

public class S3UploadException extends CustomException {
    public S3UploadException(ErrorCode errorCode) {
        super(errorCode);
    }
}
