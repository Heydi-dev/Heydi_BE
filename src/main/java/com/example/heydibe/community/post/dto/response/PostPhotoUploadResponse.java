package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostPhotoUploadResponse {

    private List<Photo> photos;

    @Getter
    @AllArgsConstructor
    public static class Photo {
        private Long fileId;
        private String fileUrl;
    }
}
