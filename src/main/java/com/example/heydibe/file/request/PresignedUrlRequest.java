package com.example.heydibe.file.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PresignedUrlRequest {

    @NotBlank(message = "folder is required (e.g., profiles, diaries)")
    @Size(max = 50, message = "folder length is too long")
    private String folder;

    @NotBlank(message = "contentType is required (e.g., image/png)")
    @Size(max = 100, message = "contentType length is too long")
    private String contentType;

    @NotBlank(message = "extension is required (e.g., png, jpg)")
    @Size(max = 10, message = "extension length is too long")
    private String extension;
}
