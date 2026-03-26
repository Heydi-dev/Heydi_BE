package com.example.heydibe.mypage.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.mypage.service.MyPageService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final AuthService authService;
    private final MyPageService myPageService;

    @GetMapping("/me")
    public ApiResponse getMyPage(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);
        return ApiResponse.success("마이페이지 조회 성공", myPageService.getMyPage(user.getId()));
    }
}