package com.example.heydibe.community.comment.repository;

import com.example.heydibe.community.comment.entity.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long postId);

    List<PostComment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);

    List<PostComment> findByPostIdAndDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long postId,
            LocalDateTime cursor,
            Pageable pageable
    );

    Optional<PostComment> findByIdAndPostIdAndDeletedAtIsNull(Long commentId, Long postId);

    long countByPostIdAndDeletedAtIsNull(Long postId);
}
