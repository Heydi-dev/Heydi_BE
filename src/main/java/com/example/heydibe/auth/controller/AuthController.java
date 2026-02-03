package com.example.heydibe.auth.controller;

import com.example.heydibe.auth.dto.request.CheckUsernameRequest; 
import com.example.heydibe.auth.dto.request.LoginRequest;
import com.example.heydibe.auth.dto.request.SignUpRequest;
import com.example.heydibe.auth.dto.response.CheckUsernameResponse;
import com.example.heydibe.auth.dto.response.LoginResponse;
import com.example.heydibe.auth.dto.response.SignUpResponse;
import com.example.heydibe.auth.dto.response.WithdrawResponse;
import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.success("로그인 성공", authService.login(request, session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.success("로그아웃 성공", null);
    }

    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ApiResponse<SignUpResponse> signup(
            @Valid @ModelAttribute SignUpRequest request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            HttpSession session) {
        return ApiResponse.success("회원가입 성공", authService.signup(request, profileImage));
    }

    @DeleteMapping("/withdraw")
    public ApiResponse<WithdrawResponse> withdraw(HttpSession session) {
        return ApiResponse.success("회원탈퇴 성공", authService.withdraw(session));
    }

    @PostMapping("/check-username")
    public ApiResponse<CheckUsernameResponse> checkUsername(@Valid @RequestBody CheckUsernameRequest request) {
        CheckUsernameResponse response = authService.checkUsername(request);
        return ApiResponse.success(response.getMessage(), response);
    }
}
