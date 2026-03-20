package com.example.heydibe.diary.repository;

import com.example.heydibe.diary.entity.DiaryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DiaryAttachmentRepository extends JpaRepository<DiaryAttachment, Long> {

    @Query("SELECT da FROM DiaryAttachment da WHERE da.diary.id = :diaryId")
    List<DiaryAttachment> findAllByDiaryId(Long diaryId);

    @Query("SELECT da FROM DiaryAttachment da WHERE da.diary.id = :diaryId ORDER BY da.id ASC")
    List<DiaryAttachment> findByDiaryIdOrderByIdAsc(Long diaryId);

    @Query("SELECT da FROM DiaryAttachment da WHERE da.id = :id AND da.diary.id = :diaryId")
    Optional<DiaryAttachment> findByIdAndDiaryId(Long id, Long diaryId);

    @Query("SELECT COUNT(da) FROM DiaryAttachment da WHERE da.diary.id = :diaryId")
    long countByDiaryId(Long diaryId);
}
