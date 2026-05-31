package com.example.heydibe.community.post.controller;

import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.community.post.dto.request.PostCreateRequest;
import com.example.heydibe.community.post.dto.request.PostPhotoUploadRequest;
import com.example.heydibe.community.post.dto.request.PostSelectDiaryRequest;
import com.example.heydibe.community.post.dto.response.PostCreateResponse;
import com.example.heydibe.community.post.dto.response.PostDetailResponse;
import com.example.heydibe.community.post.dto.response.PostFeedResponse;
import com.example.heydibe.community.post.dto.response.PostLikeResponse;
import com.example.heydibe.community.post.dto.response.PostPhotoUploadResponse;
import com.example.heydibe.community.post.dto.response.PostSelectDiaryResponse;
import com.example.heydibe.community.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityPostController {

    private final PostService postService;

    @PostMapping("/selectdiary")
    public ApiResponse<PostSelectDiaryResponse> selectDiary(
            @Valid @RequestBody PostSelectDiaryRequest request,
            @AuthUser Long userId
    ) {
        PostSelectDiaryResponse response = postService.selectDiary(userId, request.getDiaryId());
        return ApiResponse.success("포스트 초안 생성 성공", response);
    }

    @PostMapping("/posts/{postId}")
    public ApiResponse<PostCreateResponse> publishPost(
            @PathVariable Long postId,
            @Valid @RequestBody PostCreateRequest request,
            @AuthUser Long userId
    ) {
        PostCreateResponse response = postService.publishPost(userId, postId, request);
        return ApiResponse.success("포스트 저장 성공", response);
    }

    @PostMapping(value = "/posts/{postId}/photos", consumes = "multipart/form-data")
    public ApiResponse<PostPhotoUploadResponse> uploadPhoto(
            @PathVariable Long postId,
            @ModelAttribute @Valid PostPhotoUploadRequest request,
            @AuthUser Long userId
    ) {
        PostPhotoUploadResponse response = postService.addPhotos(userId, postId, request.getPhotos());
        return ApiResponse.success("사진 업로드 성공", response);
    }

    @DeleteMapping("/posts/{postId}/photos/{fileId}")
    public ApiResponse<Void> deletePhoto(
            @PathVariable Long postId,
            @PathVariable Long fileId,
            @AuthUser Long userId
    ) {
        postService.deletePhoto(userId, postId, fileId);
        return ApiResponse.success("사진 삭제 성공", null);
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long postId,
            @AuthUser Long userId
    ) {
        postService.deletePost(userId, postId);
        return ApiResponse.success("게시글 삭제 성공", null);
    }

    @GetMapping("/posts")
    public ApiResponse<PostFeedResponse> getPostFeed(@AuthUser Long userId) {
        PostFeedResponse response = postService.getPostFeed(userId);
        return ApiResponse.success("포스트 목록 조회 성공", response);
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long postId,
            @AuthUser Long userId
    ) {
        PostDetailResponse response = postService.getPostDetail(userId, postId);
        return ApiResponse.success("포스트 상세 조회 성공", response);
    }

    @PostMapping("/posts/{postId}/likes")
    public ApiResponse<PostLikeResponse> togglePostLike(
            @PathVariable Long postId,
            @AuthUser Long userId
    ) {
        PostLikeResponse response = postService.togglePostLike(userId, postId);
        String message = response.isLiked() ? "좋아요를 눌렀습니다." : "";
        return ApiResponse.success(message, response);
    }
}
