package com.example.heydibe.file.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {

    private String uploadUrl;
    private String objectKey;

    public static PresignedUrlResponse from(String uploadUrl, String objectKey) {
        return new PresignedUrlResponse(uploadUrl, objectKey);
    }
}

