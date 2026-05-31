package com.example.heydibe.community.comment.controller;

import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.community.comment.dto.request.PostCommentCreateRequest;
import com.example.heydibe.community.comment.dto.request.PostCommentUpdateRequest;
import com.example.heydibe.community.comment.dto.response.PostCommentCreateResponse;
import com.example.heydibe.community.comment.dto.response.PostCommentListResponse;
import com.example.heydibe.community.comment.dto.response.PostCommentUpdateResponse;
import com.example.heydibe.community.comment.service.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class PostCommentController {

    private final PostCommentService postCommentService;

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<PostCommentCreateResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody PostCommentCreateRequest request,
            @AuthUser Long userId
    ) {
        PostCommentCreateResponse response = postCommentService.createComment(userId, postId, request);
        return ApiResponse.success("댓글 작성 성공", response);
    }

    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<PostCommentUpdateResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody PostCommentUpdateRequest request,
            @AuthUser Long userId
    ) {
        PostCommentUpdateResponse response =
                postCommentService.updateComment(userId, postId, commentId, request);
        return ApiResponse.success("댓글 수정 성공", response);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthUser Long userId
    ) {
        postCommentService.deleteComment(userId, postId, commentId);
        return ApiResponse.success("댓글 삭제 성공", null);
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PostCommentListResponse> getCommentList(
            @PathVariable Long postId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @AuthUser Long userId
    ) {
        PostCommentListResponse response = postCommentService.getCommentList(userId, postId, cursor, size);
        return ApiResponse.success("댓글 목록 조회 성공", response);
    }
}
