package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostPhotoUploadResponse {
    private Long file_id;
    private String file_url;
}
