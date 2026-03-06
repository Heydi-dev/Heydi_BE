package com.example.heydibe.diary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.heydibe.diary.entity.DiaryAttachment;

public interface DiaryAttachmentRepository extends JpaRepository<DiaryAttachment, Long> {
    List<DiaryAttachment> findAllByDiaryId(Long diaryId);
}
