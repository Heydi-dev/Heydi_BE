package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostLikeResponse {
    private boolean is_liked;
    private int like_count;

    public boolean isLiked() {
        return is_liked;
    }
}
