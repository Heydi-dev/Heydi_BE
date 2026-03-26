package com.example.heydibe.community.post.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.community.post.dto.request.PostCreateRequest;
import com.example.heydibe.community.post.dto.response.PostCreateResponse;
import com.example.heydibe.community.post.dto.response.PostDetailResponse;
import com.example.heydibe.community.post.dto.response.PostFeedResponse;
import com.example.heydibe.community.post.dto.response.PostLikeResponse;
import com.example.heydibe.community.post.dto.response.PostPhotoUploadResponse;
import com.example.heydibe.community.post.dto.response.PostSelectDiaryResponse;
import com.example.heydibe.community.post.entity.Post;
import com.example.heydibe.community.post.entity.PostAttachment;
import com.example.heydibe.community.post.entity.PostLike;
import com.example.heydibe.community.post.repository.PostAttachmentRepository;
import com.example.heydibe.community.post.repository.PostLikeRepository;
import com.example.heydibe.community.post.repository.PostRepository;
import com.example.heydibe.infrastructure.s3.S3Service;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final int MAX_PHOTOS = 4;

    private final PostRepository postRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final S3Service s3Service;

    @Transactional
    public PostSelectDiaryResponse selectDiary(Long userId, Long diaryId) {
        if (postRepository.existsByDiaryIdAndDeletedAtIsNull(diaryId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Post post = Post.builder()
                .userId(userId)
                .diaryId(diaryId)
                .status(STATUS_DRAFT)
                .likeCount(0)
                .commentCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();

        Post saved = postRepository.save(post);
        return new PostSelectDiaryResponse(saved.getId());
    }

    @Transactional
    public PostCreateResponse publishPost(Long userId, Long postId, PostCreateRequest request) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validateOwner(userId, post);
        if (!post.isDraft()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if (!post.getDiaryId().equals(request.getDiary_id())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        int existingCount = (int) postAttachmentRepository.countByPostId(postId);
        int diaryPhotoCount = request.getExisting_photos() == null ? 0 : request.getExisting_photos().size();
        if (existingCount + diaryPhotoCount > MAX_PHOTOS) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if (request.getExisting_photos() != null) {
            for (PostCreateRequest.ExistingPhoto photo : request.getExisting_photos()) {
                String copiedUrl = s3Service.copyPostImageFromUrl(photo.getImage_url());
                saveAttachment(postId, copiedUrl);
            }
        }

        String topic = joinTopics(request.getPost_topics());
        post.publish(
                request.getPost_title(),
                topic,
                request.getPost_emotion(),
                request.getPost_content(),
                request.getDiary_date()
        );
        post.updateCommentCount(0);
        postRepository.save(post);

        return buildPostCreateResponse(post, userId);
    }

    @Transactional
    public PostPhotoUploadResponse addPhoto(Long userId, Long postId, MultipartFile photo) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validateOwner(userId, post);

        long count = postAttachmentRepository.countByPostId(postId);
        if (count >= MAX_PHOTOS) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        String fileUrl = s3Service.uploadPostImage(photo);
        PostAttachment attachment = saveAttachment(postId, fileUrl);

        return new PostPhotoUploadResponse(attachment.getId(), attachment.getFileUrl());
    }

    @Transactional
    public void deletePhoto(Long userId, Long postId, Long fileId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        validateOwner(userId, post);

        PostAttachment attachment = postAttachmentRepository.findByIdAndPostId(fileId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        s3Service.deletePostImage(attachment.getFileUrl());
        postAttachmentRepository.delete(attachment);
    }

    @Transactional
    public PostFeedResponse getPostFeed(Long userId) {
        List<Post> posts = postRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(STATUS_PUBLISHED);
        List<PostFeedResponse.PostSummary> summaries = new ArrayList<>();

        for (Post post : posts) {
            User author = userRepository.findByIdAndDeletedAtIsNull(post.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            String profileUrl = userProfileRepository.findByUserId(author.getId())
                    .map(UserProfile::getProfileImageUrl)
                    .orElse(null);

            boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), userId);
            List<String> topics = splitTopics(post.getTopic());

            summaries.add(new PostFeedResponse.PostSummary(
                    post.getId(),
                    author.getId(),
                    author.getNickname(),
                    profileUrl,
                    post.getTitle(),
                    topics,
                    post.getEmotion(),
                    post.getContent(),
                    post.getLikeCount(),
                    post.getCommentCount(),
                    isLiked,
                    post.getCreatedAt()
            ));
        }

        PostFeedResponse.Result result = new PostFeedResponse.Result(summaries, null, false);
        return new PostFeedResponse(result);
    }

    @Transactional
    public PostLikeResponse togglePostLike(Long userId, Long postId) {
        Post post = postRepository.findByIdAndStatusAndDeletedAtIsNull(postId, STATUS_PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        PostLike existing = postLikeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        if (existing == null) {
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeRepository.save(like);
            post.increaseLikeCount();
            postRepository.save(post);
            return new PostLikeResponse(true, post.getLikeCount());
        }

        postLikeRepository.delete(existing);
        post.decreaseLikeCount();
        postRepository.save(post);
        return new PostLikeResponse(false, post.getLikeCount());
    }

    @Transactional
    public PostDetailResponse getPostDetail(Long userId, Long postId) {
        Post post = postRepository.findByIdAndStatusAndDeletedAtIsNull(postId, STATUS_PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        User author = userRepository.findByIdAndDeletedAtIsNull(post.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String profileUrl = userProfileRepository.findByUserId(author.getId())
                .map(UserProfile::getProfileImageUrl)
                .orElse(null);

        List<PostAttachment> attachments = postAttachmentRepository.findByPostIdOrderByIdAsc(postId);
        List<PostDetailResponse.Photo> photos = new ArrayList<>();
        for (int i = 0; i < attachments.size(); i++) {
            PostAttachment attachment = attachments.get(i);
            photos.add(new PostDetailResponse.Photo(
                    attachment.getId(),
                    attachment.getFileUrl(),
                    i + 1
            ));
        }

        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        return new PostDetailResponse(
                post.getId(),
                new PostDetailResponse.Author(author.getId(), author.getNickname(), profileUrl),
                post.getDiaryId(),
                post.getDiaryDate(),
                post.getTitle(),
                splitTopics(post.getTopic()),
                post.getEmotion(),
                post.getContent(),
                photos,
                post.getLikeCount(),
                post.getCommentCount(),
                isLiked,
                post.getCreatedAt()
        );
    }

    private PostCreateResponse buildPostCreateResponse(Post post, Long userId) {
        List<PostAttachment> attachments = postAttachmentRepository.findByPostIdOrderByIdAsc(post.getId());
        List<PostCreateResponse.Photo> photos = attachments.stream()
                .map(attachment -> new PostCreateResponse.Photo(attachment.getId(), attachment.getFileUrl()))
                .collect(Collectors.toList());

        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), userId);

        return new PostCreateResponse(
                post.getId(),
                post.getUserId(),
                post.getDiaryId(),
                post.getTitle(),
                post.getDiaryDate(),
                splitTopics(post.getTopic()),
                post.getEmotion(),
                post.getContent(),
                photos,
                post.getLikeCount(),
                post.getCommentCount(),
                isLiked,
                post.getCreatedAt()
        );
    }

    private PostAttachment saveAttachment(Long postId, String fileUrl) {
        PostAttachment attachment = PostAttachment.builder()
                .postId(postId)
                .fileUrl(fileUrl)
                .build();
        return postAttachmentRepository.save(attachment);
    }

    private void validateOwner(Long userId, Post post) {
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private String joinTopics(List<String> topics) {
        if (topics == null || topics.isEmpty()) {
            return null;
        }
        return topics.stream()
                .map(String::trim)
                .filter(topic -> !topic.isBlank())
                .collect(Collectors.joining(","));
    }

    private List<String> splitTopics(String topic) {
        if (topic == null || topic.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(topic.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }
}
