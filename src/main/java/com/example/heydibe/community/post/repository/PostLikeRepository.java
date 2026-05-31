package com.example.heydibe.community.post.repository;

import com.example.heydibe.community.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Query("""
            SELECT pl.postId
            FROM PostLike pl
            WHERE pl.userId = :userId
              AND pl.postId IN :postIds
            """)
    Set<Long> findPostIdByUserIdAndPostIdIn(
            @Param("userId") Long userId,
            @Param("postIds") Collection<Long> postIds
    );

    int deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);
}