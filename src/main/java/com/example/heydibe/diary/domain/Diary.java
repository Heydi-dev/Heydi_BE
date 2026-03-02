package com.example.heydibe.diary.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "diary",
        indexes = {
                @Index(name = "idx_diary_user_created_at", columnList = "user_id, created_at")
        }
)
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long diaryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "main_emotion", length = 50)
    private String mainEmotion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Diary() {}

    public Long getDiaryId() { return diaryId; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMainEmotion() { return mainEmotion; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
