package com.example.heydibe.community.comment.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.community.comment.dto.request.PostCommentCreateRequest;
import com.example.heydibe.community.comment.dto.request.PostCommentUpdateRequest;
import com.example.heydibe.community.comment.dto.response.PostCommentCreateResponse;
import com.example.heydibe.community.comment.dto.response.PostCommentListResponse;
import com.example.heydibe.community.comment.dto.response.PostCommentUpdateResponse;
import com.example.heydibe.community.comment.entity.PostComment;
import com.example.heydibe.community.comment.repository.PostCommentRepository;
import com.example.heydibe.community.post.entity.Post;
import com.example.heydibe.community.post.repository.PostRepository;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public PostCommentCreateResponse createComment(Long userId, Long postId, PostCommentCreateRequest request) {
        Post post = getPublishedPost(postId);
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        PostComment comment = PostComment.builder()
                .postId(postId)
                .userId(userId)
                .commentText(request.getContent())
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

        PostComment saved = postCommentRepository.save(comment);
        post.updateCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return new PostCommentCreateResponse(
                saved.getId(),
                user.getId(),
                user.getNickname(),
                getProfileUrl(userId),
                saved.getCommentText(),
                true,
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional
    public PostCommentUpdateResponse updateComment(
            Long userId,
            Long postId,
            Long commentId,
            PostCommentUpdateRequest request
    ) {
        PostComment comment = postCommentRepository.findByIdAndPostIdAndDeletedAtIsNull(commentId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        validateOwner(userId, comment);
        getPublishedPost(postId);

        comment.updateContent(request.getContent());
        postCommentRepository.save(comment);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new PostCommentUpdateResponse(
                comment.getId(),
                user.getId(),
                user.getNickname(),
                getProfileUrl(userId),
                comment.getCommentText(),
                true,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
        PostComment comment = postCommentRepository.findByIdAndPostIdAndDeletedAtIsNull(commentId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        validateOwner(userId, comment);

        postCommentRepository.delete(comment);

        Post post = getPublishedPost(postId);
        int updatedCount = Math.max(0, post.getCommentCount() - 1);
        post.updateCommentCount(updatedCount);
        postRepository.save(post);
    }

    @Transactional
    public PostCommentListResponse getCommentList(Long userId, Long postId, LocalDateTime cursor, Integer size) {
        getPublishedPost(postId);

        int pageSize = size == null ? 10 : size;
        if (pageSize <= 0) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);
        List<PostComment> fetched = cursor == null
                ? postCommentRepository.findByPostIdAndDeletedAtIsNullOrderByCreatedAtDesc(postId, pageRequest)
                : postCommentRepository.findByPostIdAndDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(
                postId,
                cursor,
                pageRequest
        );

        boolean hasNext = fetched.size() > pageSize;
        List<PostComment> comments = hasNext ? fetched.subList(0, pageSize) : fetched;

        List<PostCommentListResponse.Comment> results = new ArrayList<>();
        for (PostComment comment : comments) {
            User author = userRepository.findByIdAndDeletedAtIsNull(comment.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            boolean isMine = comment.getUserId().equals(userId);

            results.add(new PostCommentListResponse.Comment(
                    comment.getId(),
                    author.getId(),
                    author.getNickname(),
                    getProfileUrl(author.getId()),
                    comment.getCommentText(),
                    isMine,
                    comment.getCreatedAt()
            ));
        }

        LocalDateTime nextCursor = null;
        if (hasNext && !comments.isEmpty()) {
            nextCursor = comments.get(comments.size() - 1).getCreatedAt();
        }

        PostCommentListResponse.Result result = new PostCommentListResponse.Result(results, nextCursor, hasNext);
        return new PostCommentListResponse(result);
    }

    private void validateOwner(Long userId, PostComment comment) {
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private Post getPublishedPost(Long postId) {
        return postRepository.findByIdAndStatusAndDeletedAtIsNull(postId, STATUS_PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private String getProfileUrl(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getProfileImageUrl)
                .orElse(null);
    }
}
