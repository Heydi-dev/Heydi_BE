package com.example.heydibe.diary.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.user.entity.User;

public interface DiaryRepository extends JpaRepository<Diary, Long>{
    Page<Diary> findAllByUser(User user, Pageable pageable);
}
