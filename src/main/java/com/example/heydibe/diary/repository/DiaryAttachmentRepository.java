package com.example.heydibe.diary.repository;

import com.example.heydibe.diary.domain.DiaryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryAttachmentRepository extends JpaRepository<DiaryAttachment, Long> {

    List<DiaryAttachment> findByDiaryIdOrderByIdAsc(Long diaryId);

    Optional<DiaryAttachment> findByIdAndDiaryId(Long id, Long diaryId);

    long countByDiaryId(Long diaryId);
}
