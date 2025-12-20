package com.example.heydibe.auth.controller;

import com.example.heydibe.auth.request.CheckUsernameRequest; 
import com.example.heydibe.auth.request.LoginRequest;
import com.example.heydibe.auth.response.CheckUsernameResponse;
import com.example.heydibe.auth.response.LoginResponse;    
import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.success(authService.login(request, session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.success(null);
    }

    @PostMapping("/check-username")
    public ApiResponse<CheckUsernameResponse> checkUsername(@Valid @RequestBody CheckUsernameRequest request) {
        return ApiResponse.success(authService.checkUsername(request));
    }
}
