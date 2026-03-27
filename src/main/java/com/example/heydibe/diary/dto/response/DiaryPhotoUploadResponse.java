package com.example.heydibe.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryPhotoUploadResponse {

    private List<Photo> photos;

    @Getter
    @AllArgsConstructor
    public static class Photo {
        private Long id;
        private String imageUrl;
    }
}
