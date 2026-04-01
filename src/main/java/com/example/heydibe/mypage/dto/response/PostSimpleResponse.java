package com.example.heydibe.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostSimpleResponse {

    private Long postId;
    private String content;
    private int likeCount;
    private boolean liked;
}