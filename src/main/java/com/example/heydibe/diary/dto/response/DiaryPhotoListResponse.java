package com.example.heydibe.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryPhotoListResponse {

    private List<Photo> photos;

    @Getter
    @AllArgsConstructor
    public static class Photo {
        private Long fileId;
        private String fileUrl;
        private int order;
    }
}
