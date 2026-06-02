package com.example.heydibe.mypage.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.mypage.service.MyPageService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyPagePostController {

    private final AuthService authService;
    private final MyPageService myPageService;

    @GetMapping("/mypage/shared")
    public ApiResponse getMySharedPosts(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "내가 공유한 글 목록을 불러왔습니다.",
                myPageService.getMyPosts(user.getId(), page, size)
        );
    }

    @GetMapping("/mypage/likes")
    public ApiResponse getMyLikedPosts(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "좋아요한 글 목록을 불러왔습니다.",
                myPageService.getLikedPosts(user.getId(), page, size)
        );
    }
}