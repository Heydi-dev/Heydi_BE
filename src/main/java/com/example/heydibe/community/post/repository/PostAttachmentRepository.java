package com.example.heydibe.community.post.repository;

import com.example.heydibe.community.post.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
    List<PostAttachment> findByPostIdOrderByIdAsc(Long postId);

    Optional<PostAttachment> findByIdAndPostId(Long id, Long postId);

    long countByPostId(Long postId);

    Optional<PostAttachment> findTopByPostIdOrderByIdAsc(Long postId);
}
