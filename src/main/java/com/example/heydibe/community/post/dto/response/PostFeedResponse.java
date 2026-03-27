package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostFeedResponse {

    private Result feed;

    @Getter
    @AllArgsConstructor
    public static class Result {
        private List<PostSummary> posts;
        private Long nextCursor;
        private boolean hasNext;
    }

    @Getter
    @AllArgsConstructor
    public static class PostSummary {
        private Long postId;
        private Long userId;
        private String nickname;
        private String profileUrl;
        private String postTitle;
        private List<String> postTopics;
        private String postEmotion;
        private String postContent;
        private int likeCount;
        private int commentCount;
        private boolean liked;
        private LocalDateTime createdAt;
    }
}
