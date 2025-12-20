package com.example.heydibe.file.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PresignedUrlRequest {

    @NotBlank(message = "folder는 필수입니다 (예: profiles, diaries)")
    @Size(max = 50, message = "folder 길이가 너무 깁니다")
    private String folder;

    @NotBlank(message = "contentType은 필수입니다 (예: image/png)")
    @Size(max = 100, message = "contentType 길이가 너무 깁니다")
    private String contentType;

    @NotBlank(message = "extension은 필수입니다 (예: png, jpg)")
    @Size(max = 10, message = "extension 길이가 너무 깁니다")
    private String extension;
}
