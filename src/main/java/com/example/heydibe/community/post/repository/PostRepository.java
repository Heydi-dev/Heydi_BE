package com.example.heydibe.community.post.repository;

import com.example.heydibe.community.post.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByDiaryIdAndDeletedAtIsNull(Long diaryId);

    Optional<Post> findByIdAndDeletedAtIsNull(Long postId);

    Optional<Post> findByIdAndStatusAndDeletedAtIsNull(Long postId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId and p.status = :status and p.deletedAt is null")
    Optional<Post> findForLikeToggle(@Param("postId") Long postId, @Param("status") String status);

    List<Post> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(String status);
}
