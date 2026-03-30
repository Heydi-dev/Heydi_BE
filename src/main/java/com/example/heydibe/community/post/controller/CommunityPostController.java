package com.example.heydibe.community.post.controller;

import com.example.heydibe.auth.service.AuthService;
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
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class PostController {

    private final AuthService authService;
    private final PostService postService;

    @PostMapping("/selectdiary")
    public ApiResponse<PostSelectDiaryResponse> selectDiary(
            @Valid @RequestBody PostSelectDiaryRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        PostSelectDiaryResponse response = postService.selectDiary(user.getId(), request.getDiaryId());
        return ApiResponse.success("포스트 초안 생성 성공", response);
    }

    @PostMapping("/posts/{postId}")
    public ApiResponse<PostCreateResponse> publishPost(
            @PathVariable Long postId,
            @Valid @RequestBody PostCreateRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        PostCreateResponse response = postService.publishPost(user.getId(), postId, request);
        return ApiResponse.success("포스트 저장 성공", response);
    }

    @PostMapping(value = "/posts/{postId}/photos", consumes = "multipart/form-data")
    public ApiResponse<PostPhotoUploadResponse> uploadPhoto(
            @PathVariable Long postId,
            @ModelAttribute @Valid PostPhotoUploadRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        MultipartFile photo = request.getPhoto();
        PostPhotoUploadResponse response = postService.addPhoto(user.getId(), postId, photo);
        return ApiResponse.success("사진 업로드 성공", response);
    }

    @DeleteMapping("/posts/{postId}/photos/{fileId}")
    public ApiResponse<Void> deletePhoto(
            @PathVariable Long postId,
            @PathVariable Long fileId,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        postService.deletePhoto(user.getId(), postId, fileId);
        return ApiResponse.success("사진 삭제 성공", null);
    }

    @GetMapping("/posts")
    public ApiResponse<PostFeedResponse> getPostFeed(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);
        PostFeedResponse response = postService.getPostFeed(user.getId());
        return ApiResponse.success("포스트 목록 조회 성공", response);
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long postId,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        PostDetailResponse response = postService.getPostDetail(user.getId(), postId);
        return ApiResponse.success("포스트 상세 조회 성공", response);
    }

    @PostMapping("/posts/{postId}/likes")
    public ApiResponse<PostLikeResponse> togglePostLike(
            @PathVariable Long postId,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        PostLikeResponse response = postService.togglePostLike(user.getId(), postId);
        String message = response.isLiked() ? "좋아요를 눌렀습니다." : "";
        return ApiResponse.success(message, response);
    }

}
