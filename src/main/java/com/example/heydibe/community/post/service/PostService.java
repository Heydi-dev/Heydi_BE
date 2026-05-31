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

import com.example.heydibe.community.post.repository.projection.PostDetailProjection;

import com.example.heydibe.community.post.repository.projection.PostFeedProjection;

import com.example.heydibe.diary.entity.Diary;

import com.example.heydibe.diary.entity.DiaryAttachment;

import com.example.heydibe.diary.repository.DiaryAttachmentRepository;

import com.example.heydibe.diary.repository.DiaryRepository;

import com.example.heydibe.infrastructure.s3.S3Service;

import com.example.heydibe.user.dto.UserPublicProfile;

import com.example.heydibe.user.service.UserPublicProfileService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;

import java.util.List;

import java.util.Map;

import java.util.Set;

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

    private final DiaryRepository diaryRepository;

    private final DiaryAttachmentRepository diaryAttachmentRepository;

    private final S3Service s3Service;

    private final UserPublicProfileService userPublicProfileService;



    @Transactional

    public PostSelectDiaryResponse selectDiary(Long userId, Long diaryId) {

        validateOwnedDiary(userId, diaryId);



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



        if (!post.getDiaryId().equals(request.getDiaryId())) {

            throw new CustomException(ErrorCode.BAD_REQUEST);

        }



        int existingCount = (int) postAttachmentRepository.countByPostId(postId);

        int diaryPhotoCount = request.getExistingPhotos() == null ? 0 : request.getExistingPhotos().size();

        if (existingCount + diaryPhotoCount > MAX_PHOTOS) {

            throw new CustomException(ErrorCode.BAD_REQUEST);

        }



        validateDiaryPhotoUrls(userId, post.getDiaryId(), request.getExistingPhotos());



        List<String> copiedUrls = new ArrayList<>();

        try {

            if (request.getExistingPhotos() != null) {

                for (PostCreateRequest.ExistingPhoto photo : request.getExistingPhotos()) {

                    String copiedUrl = s3Service.copyPostImageFromUrl(photo.getImageUrl());

                    copiedUrls.add(copiedUrl);

                    saveAttachment(postId, copiedUrl);

                }

            }

        } catch (RuntimeException e) {

            copiedUrls.forEach(s3Service::deletePostImage);

            throw e;

        }



        String topic = joinTopics(request.getPostTopics());

        post.publish(

                request.getPostTitle(),

                topic,

                request.getPostEmotion(),

                request.getPostContent(),

                request.getDiaryDate()

        );

        post.updateCommentCount(0);

        postRepository.save(post);



        return buildPostCreateResponse(post, userId);

    }



    @Transactional

    public PostPhotoUploadResponse addPhotos(Long userId, Long postId, List<MultipartFile> photos) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)

                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));



        validateOwner(userId, post);

        validateDraft(post);

        validatePhotosInput(photos);



        long count = postAttachmentRepository.countByPostId(postId);

        if (count + photos.size() > MAX_PHOTOS) {

            throw new CustomException(ErrorCode.BAD_REQUEST);

        }



        List<PostPhotoUploadResponse.Photo> uploadedPhotos = new ArrayList<>();

        List<String> uploadedUrls = new ArrayList<>();

        try {

            for (MultipartFile photo : photos) {

                String fileUrl = s3Service.uploadPostImage(photo);

                uploadedUrls.add(fileUrl);

                PostAttachment attachment = saveAttachment(postId, fileUrl);

                uploadedPhotos.add(new PostPhotoUploadResponse.Photo(

                        attachment.getId(),

                        s3Service.resolvePublicUrl(attachment.getFileUrl())

                ));

            }

        } catch (RuntimeException e) {

            uploadedUrls.forEach(s3Service::deletePostImage);

            throw e;

        }



        return new PostPhotoUploadResponse(uploadedPhotos);

    }



    @Transactional

    public void deletePhoto(Long userId, Long postId, Long fileId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)

                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validateOwner(userId, post);

        validateDraft(post);



        PostAttachment attachment = postAttachmentRepository.findByIdAndPostId(fileId, postId)

                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));



        s3Service.deletePostImage(attachment.getFileUrl());

        postAttachmentRepository.delete(attachment);

    }



    @Transactional

    public void deletePost(Long userId, Long postId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)

                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validateOwner(userId, post);

        if (post.isDraft()) {

            removeDraftAttachments(postId);

            postRepository.delete(post);

            return;

        }

        post.softDelete();

        postRepository.save(post);

    }



    private void removeDraftAttachments(Long postId) {

        List<PostAttachment> attachments = postAttachmentRepository.findByPostIdOrderByIdAsc(postId);

        for (PostAttachment attachment : attachments) {

            s3Service.deletePostImage(attachment.getFileUrl());

        }

        if (!attachments.isEmpty()) {

            postAttachmentRepository.deleteAll(attachments);

        }

    }



    @Transactional

    public PostFeedResponse getPostFeed(Long userId) {

        List<PostFeedProjection> rows = postRepository.findPublishedFeed(STATUS_PUBLISHED);

        if (rows.isEmpty()) {

            PostFeedResponse.Result empty = new PostFeedResponse.Result(Collections.emptyList(), null, false);

            return new PostFeedResponse(empty);

        }



        List<Long> postIds = rows.stream().map(PostFeedProjection::getPostId).toList();

        Set<Long> likedPostIds = postLikeRepository.findPostIdByUserIdAndPostIdIn(userId, postIds);



        List<Long> authorIds = rows.stream().map(PostFeedProjection::getUserId).distinct().toList();

        Map<Long, UserPublicProfile> authors = userPublicProfileService.resolveAll(authorIds);



        List<PostFeedResponse.PostSummary> summaries = new ArrayList<>(rows.size());

        for (PostFeedProjection row : rows) {

            UserPublicProfile author = authors.get(row.getUserId());

            summaries.add(new PostFeedResponse.PostSummary(

                    row.getPostId(),

                    author.userId(),

                    author.nickname(),

                    author.profileImageUrl(),

                    row.getPostTitle(),

                    splitTopics(row.getTopic()),

                    row.getPostEmotion(),

                    row.getPostContent(),

                    row.getLikeCount(),

                    row.getCommentCount(),

                    likedPostIds.contains(row.getPostId()),

                    row.getCreatedAt()

            ));

        }



        PostFeedResponse.Result result = new PostFeedResponse.Result(summaries, null, false);

        return new PostFeedResponse(result);

    }



    @Transactional

    public PostLikeResponse togglePostLike(Long userId, Long postId) {

        Post post = postRepository.findForLikeToggle(postId, STATUS_PUBLISHED)

                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));



        boolean liked;

        int deleted = postLikeRepository.deleteByPostIdAndUserId(postId, userId);

        if (deleted == 1) {

            liked = false;

        } else {

            try {

                PostLike like = PostLike.builder()

                        .postId(postId)

                        .userId(userId)

                        .createdAt(LocalDateTime.now())

                        .build();

                postLikeRepository.save(like);

                liked = true;

            } catch (DataIntegrityViolationException e) {

                liked = true;

            }

        }



        long likeCount = postLikeRepository.countByPostId(postId);

        post.syncLikeCount((int) likeCount);

        postRepository.save(post);

        return new PostLikeResponse(liked, post.getLikeCount());

    }



    @Transactional

    public PostDetailResponse getPostDetail(Long userId, Long postId) {

        PostDetailProjection row = postRepository.findPublishedDetail(postId, STATUS_PUBLISHED)

                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));



        UserPublicProfile author = userPublicProfileService.resolve(row.getUserId());



        List<PostAttachment> attachments = postAttachmentRepository.findByPostIdOrderByIdAsc(postId);

        List<PostDetailResponse.Photo> photos = new ArrayList<>();

        for (int i = 0; i < attachments.size(); i++) {

            PostAttachment attachment = attachments.get(i);

            photos.add(new PostDetailResponse.Photo(

                    attachment.getId(),

                    s3Service.resolvePublicUrl(attachment.getFileUrl()),

                    i + 1

            ));

        }



        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);



        return new PostDetailResponse(

                row.getPostId(),

                new PostDetailResponse.Author(

                        author.userId(),

                        author.nickname(),

                        author.profileImageUrl()

                ),

                row.getDiaryId(),

                row.getDiaryDate(),

                row.getPostTitle(),

                splitTopics(row.getTopic()),

                row.getPostEmotion(),

                row.getPostContent(),

                photos,

                row.getLikeCount(),

                row.getCommentCount(),

                isLiked,

                row.getCreatedAt()

        );

    }



    private PostCreateResponse buildPostCreateResponse(Post post, Long userId) {

        List<PostAttachment> attachments = postAttachmentRepository.findByPostIdOrderByIdAsc(post.getId());

        List<PostCreateResponse.Photo> photos = attachments.stream()

                .map(attachment -> new PostCreateResponse.Photo(

                        attachment.getId(),

                        s3Service.resolvePublicUrl(attachment.getFileUrl())

                ))

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



    private void validateOwnedDiary(Long userId, Long diaryId) {

        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)

                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));



        if (!diary.getUser().getId().equals(userId)) {

            throw new CustomException(ErrorCode.ACCESS_DENIED);

        }

    }



    private void validateDiaryPhotoUrls(

            Long userId,

            Long diaryId,

            List<PostCreateRequest.ExistingPhoto> photos

    ) {

        if (photos == null || photos.isEmpty()) {

            return;

        }



        validateOwnedDiary(userId, diaryId);



        List<DiaryAttachment> attachments = diaryAttachmentRepository.findByDiaryIdOrderByIdAsc(diaryId);

        for (PostCreateRequest.ExistingPhoto photo : photos) {

            boolean matched = attachments.stream()

                    .anyMatch(attachment -> matchesDiaryAttachmentUrl(photo.getImageUrl(), attachment));

            if (!matched) {

                throw new CustomException(ErrorCode.FORBIDDEN);

            }

        }

    }



    private boolean matchesDiaryAttachmentUrl(String requestUrl, DiaryAttachment attachment) {

        String normalizedRequest = s3Service.resolvePublicUrl(requestUrl);

        String normalizedStored = s3Service.resolvePublicUrl(attachment.getFileUrl());

        return normalizedRequest.equals(normalizedStored);

    }



    private void validateOwner(Long userId, Post post) {

        if (!post.getUserId().equals(userId)) {

            throw new CustomException(ErrorCode.FORBIDDEN);

        }

    }



    private void validateDraft(Post post) {

        if (!post.isDraft()) {

            throw new CustomException(ErrorCode.BAD_REQUEST);

        }

    }



    private void validatePhotosInput(List<MultipartFile> photos) {

        if (photos == null || photos.isEmpty()) {

            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);

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


