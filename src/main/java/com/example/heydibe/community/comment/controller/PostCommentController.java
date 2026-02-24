package com.example.heydibe.community.comment.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.community.comment.dto.request.PostCommentCreateRequest;
import com.example.heydibe.community.comment.dto.request.PostCommentUpdateRequest;
import com.example.heydibe.community.comment.dto.response.PostCommentCreateResponse;
import com.example.heydibe.community.comment.dto.response.PostCommentListResponse;
import com.example.heydibe.community.comment.dto.response.PostCommentUpdateResponse;
import com.example.heydibe.community.comment.service.PostCommentService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class PostCommentController {

    private final AuthService authService;
    private final PostCommentService postCommentService;

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<PostCommentCreateResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody PostCommentCreateRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        PostCommentCreateResponse response = postCommentService.createComment(user.getId(), postId, request);
        return ApiResponse.success("댓글 작성 성공", response);
    }

    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<PostCommentUpdateResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody PostCommentUpdateRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        PostCommentUpdateResponse response =
                postCommentService.updateComment(user.getId(), postId, commentId, request);
        return ApiResponse.success("댓글 수정 성공", response);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        postCommentService.deleteComment(user.getId(), postId, commentId);
        return ApiResponse.success("댓글 삭제 성공", null);
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PostCommentListResponse> getCommentList(
            @PathVariable Long postId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        PostCommentListResponse response = postCommentService.getCommentList(user.getId(), postId, cursor, size);
        return ApiResponse.success("댓글 목록 조회 성공", response);
    }
}
