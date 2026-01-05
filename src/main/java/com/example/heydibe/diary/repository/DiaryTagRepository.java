package com.example.heydibe.diary.repository;

import com.example.heydibe.diary.domain.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DiaryTagRepository extends JpaRepository<DiaryTag, Long> {

    @Query("""
           select dt
           from DiaryTag dt
           where dt.diaryId = :diaryId
             and dt.tagCategory = 'TOPIC'
           order by dt.createdAt asc
           """)
    Optional<DiaryTag> findFirstTopicTag(Long diaryId);
}
