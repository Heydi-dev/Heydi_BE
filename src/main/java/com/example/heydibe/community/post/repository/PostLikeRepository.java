package com.example.heydibe.community.post.repository;

import com.example.heydibe.community.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Set<Long> findPostIdByUserIdAndPostIdIn(Long userId, Collection<Long> postIds);

    int deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);
}