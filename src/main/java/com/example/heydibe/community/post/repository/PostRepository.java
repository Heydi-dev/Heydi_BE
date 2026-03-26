package com.example.heydibe.community.post.repository;

import com.example.heydibe.community.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByDiaryIdAndDeletedAtIsNull(Long diaryId);

    Optional<Post> findByIdAndDeletedAtIsNull(Long postId);

    Optional<Post> findByIdAndStatusAndDeletedAtIsNull(Long postId, String status);

    List<Post> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(String status);
}
