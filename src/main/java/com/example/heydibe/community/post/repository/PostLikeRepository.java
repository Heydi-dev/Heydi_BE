package com.example.heydibe.community.post.repository;

import com.example.heydibe.community.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    int deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);
}