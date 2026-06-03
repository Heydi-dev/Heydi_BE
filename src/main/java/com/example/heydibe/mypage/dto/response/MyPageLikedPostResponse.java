package com.example.heydibe.mypage.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyPageLikedPostResponse {

    private long totalCount;
    private int page;
    private int size;
    private List<LikedPostItem> posts;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LikedPostItem {

        private Long userId;
        private String nickname;
        private String profileImageUrl;

        private Long postId;
        private Long diaryId;

        private String title;
        private String preview;
        private String emotion;
        private List<String> topics;

        private LocalDateTime createdAt;

        private int likeCount;
        private int commentCount;

        @JsonProperty("isLiked")
        private boolean liked;
    }
}