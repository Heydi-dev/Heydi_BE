package com.example.heydibe.mypage.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.mypage.service.MyPageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final AuthService authService;
    private final MyPageService myPageService;

    @GetMapping("/users/me")
    public ApiResponse getMyPage(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "마이페이지 메인 조회에 성공했습니다.",
                myPageService.getMyPage(user.getId())
        );
    }
}