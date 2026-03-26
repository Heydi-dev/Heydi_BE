package com.example.heydibe.diary.entity;

import java.time.LocalDateTime;

import com.example.heydibe.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diary")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "summary_one_line", length = 255)
    private String summaryOneLine;

    @Column(name = "main_emotion", length = 50)
    private String mainEmotion;

    @Column(length = 20)
    private String topic1;

    @Column(length = 20)
    private String topic2;

    @Column(name = "emotion_score")
    private int emotionScore;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "included_in_monthly_report")
    private Boolean includedInMonthlyReport;

    @Column(name = "conversation_duration_seconds")
    private int conversationDurationSeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
