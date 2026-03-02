package com.example.heydibe.diary.repository;

import com.example.heydibe.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("""
           select d
           from Diary d
           where d.userId = :userId
             and d.deletedAt is null
             and d.createdAt >= :start
             and d.createdAt < :end
           order by d.createdAt asc
           """)
    List<Diary> findDiariesInRange(Long userId, Instant start, Instant end);

    @Query("""
           select d
           from Diary d
           where d.userId = :userId
             and d.deletedAt is null
             and d.createdAt >= :start
             and d.createdAt < :end
           order by d.createdAt desc
           """)
    Optional<Diary> findLatestDiaryInRange(Long userId, Instant start, Instant end);
}
