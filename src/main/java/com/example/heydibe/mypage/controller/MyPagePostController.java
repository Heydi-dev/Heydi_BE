package com.example.heydibe.mypage.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.mypage.service.MyPageService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class   {

    private final AuthService authService;
    private final MyPageService myPageService;

    /**
     * 내가 공유한 글 조회
     * GET /mypage/shared
     */
    @GetMapping("/mypage/shared")
    public ApiResponse getMySharedPosts(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "내가 공유한 글 조회 성공",
                myPageService.getMyPosts(user.getId())
        );
    }

    /**
     * 내가 좋아요 한 글 조회
     * GET /mypage/likes
     */
    @GetMapping("/mypage/likes")
    public ApiResponse getMyLikedPosts(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "내가 좋아요 한 글 조회 성공",
                myPageService.getLikedPosts(user.getId())
        );
    }
}