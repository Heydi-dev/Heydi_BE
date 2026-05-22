package com.example.heydibe.community.post.repository.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class PostDetailProjection {

    private final Long postId;
    private final Long userId;
    private final String nickname;
    private final String profileUrl;
    private final Long diaryId;
    private final LocalDate diaryDate;
    private final String postTitle;
    private final String topic;
    private final String postEmotion;
    private final String postContent;
    private final int likeCount;
    private final int commentCount;
    private final LocalDateTime createdAt;

    public PostDetailProjection(
            Long postId,
            Long userId,
            String nickname,
            String profileUrl,
            Long diaryId,
            LocalDate diaryDate,
            String postTitle,
            String topic,
            String postEmotion,
            String postContent,
            int likeCount,
            int commentCount,
            LocalDateTime createdAt
    ) {
        this.postId = postId;
        this.userId = userId;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.diaryId = diaryId;
        this.diaryDate = diaryDate;
        this.postTitle = postTitle;
        this.topic = topic;
        this.postEmotion = postEmotion;
        this.postContent = postContent;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }
}
