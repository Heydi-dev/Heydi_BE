package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostDetailResponse {

    private Long postId;
    private Author author;
    private Long diaryId;
    private LocalDate diaryDate;
    private String postTitle;
    private List<String> postTopics;
    private String postEmotion;
    private String postContent;
    private List<Photo> photos;
    private int likeCount;
    private int commentCount;
    private boolean liked;
    private LocalDateTime createdAt;

    @Getter
    @AllArgsConstructor
    public static class Author {
        private Long userId;
        private String nickname;
        private String profileUrl;
    }

    @Getter
    @AllArgsConstructor
    public static class Photo {
        private Long id;
        private String imageUrl;
        private int order;
    }
}
