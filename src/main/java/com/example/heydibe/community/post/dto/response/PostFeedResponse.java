package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostFeedResponse {

    private Result result;

    @Getter
    @AllArgsConstructor
    public static class Result {
        private List<PostSummary> posts;
        private Long next_cursor;
        private boolean has_next;
    }

    @Getter
    @AllArgsConstructor
    public static class PostSummary {
        private Long post_id;
        private Long user_id;
        private String nickname;
        private String profile_url;
        private String post_title;
        private List<String> post_topics;
        private String post_emotion;
        private String post_content;
        private int like_count;
        private int comment_count;
        private boolean is_liked;
        private LocalDateTime created_at;
    }
}
