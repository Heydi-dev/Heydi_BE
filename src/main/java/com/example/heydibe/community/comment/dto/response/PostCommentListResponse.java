package com.example.heydibe.community.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostCommentListResponse {

    private Result result;

    @Getter
    @AllArgsConstructor
    public static class Result {
        private List<Comment> comments;
        private LocalDateTime next_cursor;
        private boolean has_next;
    }

    @Getter
    @AllArgsConstructor
    public static class Comment {
        private Long comment_id;
        private Long user_id;
        private String nickname;
        private String profile_url;
        private String content;
        private boolean is_mine;
        private LocalDateTime created_at;
    }
}
