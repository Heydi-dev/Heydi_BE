package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostCreateResponse {

    private Long postId;
    private Long userId;
    private Long diaryId;
    private String postTitle;
    private LocalDate diaryDate;
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
    public static class Photo {
        private Long id;
        private String imageUrl;
    }
}
