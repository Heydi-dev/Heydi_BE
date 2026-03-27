package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostPhotoUploadResponse {
    private Long fileId;
    private String fileUrl;
}
