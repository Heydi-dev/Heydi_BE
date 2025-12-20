package com.example.heydibe.file.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PresignedUrlRequest {

    @NotBlank(message = "folder???�수?�니?? (?? profiles, diaries)")
    @Size(max = 50, message = "folder 길이가 ?�무 깁니??")
    private String folder;

    @NotBlank(message = "contentType?� ?�수?�니?? (?? image/png)")
    @Size(max = 100, message = "contentType 길이가 ?�무 깁니??")
    private String contentType;

    @NotBlank(message = "extension?� ?�수?�니?? (?? png, jpg)")
    @Size(max = 10, message = "extension 길이가 ?�무 깁니??")
    private String extension;
}

