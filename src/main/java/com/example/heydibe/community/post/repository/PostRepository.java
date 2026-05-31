package com.example.heydibe.community.post.repository;

import com.example.heydibe.community.post.entity.Post;
import com.example.heydibe.community.post.repository.projection.PostDetailProjection;
import com.example.heydibe.community.post.repository.projection.PostFeedProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndDeletedAtIsNull(Long postId);

    Optional<Post> findByIdAndStatusAndDeletedAtIsNull(Long postId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId and p.status = :status and p.deletedAt is null")
    Optional<Post> findForLikeToggle(@Param("postId") Long postId, @Param("status") String status);

    List<Post> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(String status);

    @Query("""
            SELECT new com.example.heydibe.community.post.repository.projection.PostFeedProjection(
                p.id, p.userId, u.nickname, up.profileImageUrl,
                p.title, p.topic, p.emotion, p.content,
                p.likeCount, p.commentCount, p.createdAt
            )
            FROM Post p
            JOIN User u ON p.userId = u.id
            LEFT JOIN UserProfile up ON up.userId = u.id
            WHERE p.status = :status
              AND p.deletedAt IS NULL
            ORDER BY p.createdAt DESC
            """)
    List<PostFeedProjection> findPublishedFeed(@Param("status") String status);

    @Query("""
            SELECT new com.example.heydibe.community.post.repository.projection.PostDetailProjection(
                p.id, p.userId, u.nickname, up.profileImageUrl,
                p.diaryId, p.diaryDate,
                p.title, p.topic, p.emotion, p.content,
                p.likeCount, p.commentCount, p.createdAt
            )
            FROM Post p
            JOIN User u ON p.userId = u.id
            LEFT JOIN UserProfile up ON up.userId = u.id
            WHERE p.id = :postId
              AND p.status = :status
              AND p.deletedAt IS NULL
            """)
    Optional<PostDetailProjection> findPublishedDetail(
            @Param("postId") Long postId,
            @Param("status") String status
    );
}
