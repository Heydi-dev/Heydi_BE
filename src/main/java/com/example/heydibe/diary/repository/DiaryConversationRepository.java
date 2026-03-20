package com.example.heydibe.diary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.heydibe.diary.entity.DiaryConversation;

public interface DiaryConversationRepository extends JpaRepository<DiaryConversation, Long> {
    @Query("SELECT dc FROM DiaryConversation dc WHERE dc.diary.id = :diaryId ORDER BY dc.createdAt ASC")
    List<DiaryConversation> findAllByDiaryId(Long diaryId);
}
