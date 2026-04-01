package com.example.heydibe.community.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community/posts")
public class PostController {

    private final AuthService authService;

    // ✅ 게시글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse deletePost(
            @PathVariable Long postId,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);

        // TODO: 실제 삭제 로직 추가
        return ApiResponse.success("게시글 삭제 성공", null);
    }
}