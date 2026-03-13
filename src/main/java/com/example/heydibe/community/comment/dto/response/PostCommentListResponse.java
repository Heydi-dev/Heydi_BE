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
        private LocalDateTime nextCursor;
        private boolean hasNext;
    }

    @Getter
    @AllArgsConstructor
    public static class Comment {
        private Long commentId;
        private Long userId;
        private String nickname;
        private String profileUrl;
        private String content;
        private boolean mine;
        private LocalDateTime createdAt;
    }
}
