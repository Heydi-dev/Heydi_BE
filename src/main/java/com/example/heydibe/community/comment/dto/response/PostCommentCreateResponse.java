package com.example.heydibe.community.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostCommentCreateResponse {

    private Long commentId;
    private Long userId;
    private String nickname;
    private String profileUrl;
    private String content;
    private boolean mine;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
