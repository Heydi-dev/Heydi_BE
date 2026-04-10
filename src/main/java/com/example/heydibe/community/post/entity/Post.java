package com.example.heydibe.community.post.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "topic", length = 255)
    private String topic;

    @Column(name = "emotion", length = 50)
    private String emotion;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "diary_date")
    private LocalDate diaryDate;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDraft() {
        return "DRAFT".equals(this.status);
    }

    public void publish(String title, String topic, String emotion, String content, LocalDate diaryDate) {
        this.title = title;
        this.topic = topic;
        this.emotion = emotion;
        this.content = content;
        this.diaryDate = diaryDate;
        this.status = "PUBLISHED";
        this.updatedAt = LocalDateTime.now();
    }

    public void syncLikeCount(int likeCount) {
        this.likeCount = likeCount;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCommentCount(int commentCount) {
        this.commentCount = commentCount;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}