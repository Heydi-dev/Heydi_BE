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

    @Column(name = "diary_id", nullable = false, unique = true)
    private Long diaryId;

    @Column(name = "post_title", length = 100)
    private String title;

    @Column(name = "post_topic", length = 100)
    private String topic;

    @Column(name = "post_emotion", length = 50)
    private String emotion;

    @Column(name = "post_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "diary_date")
    private LocalDate diaryDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void publish(String title, String topic, String emotion, String content, LocalDate diaryDate) {
        this.title = title;
        this.topic = topic;
        this.emotion = emotion;
        this.content = content;
        this.diaryDate = diaryDate;
        this.status = "PUBLISHED";
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDraft() {
        return "DRAFT".equals(this.status);
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount -= 1;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void syncLikeCount(int likeCount) {
        this.likeCount = Math.max(likeCount, 0);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCommentCount(int commentCount) {
        this.commentCount = commentCount;
        this.updatedAt = LocalDateTime.now();
    }
}
