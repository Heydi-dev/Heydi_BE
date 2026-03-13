package com.example.heydibe.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryPhotoUploadResponse {

    private Long fileId;
    private String fileUrl;
}
