package com.example.heydibe.diary.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="diary_conversation")
public class DiaryConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, targetEntity = Diary.class)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Column(length = 10, nullable = false)
    private String sender;

    @Column(name = "message_text", nullable = false)
    private String messageText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
