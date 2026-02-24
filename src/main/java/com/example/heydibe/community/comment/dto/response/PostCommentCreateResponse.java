package com.example.heydibe.community.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostCommentCreateResponse {

    private Long comment_id;
    private Long user_id;
    private String nickname;
    private String profile_url;
    private String content;
    private boolean is_mine;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
