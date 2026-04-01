package com.example.heydibe.settings.controller;

import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.settings.dto.request.FcmTokenRegisterRequest;
import com.example.heydibe.settings.service.FcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/fcmtoken")
    public ApiResponse<Void> registerFcmToken(
            @AuthUser Long userId,
            @Valid @RequestBody FcmTokenRegisterRequest request
    ) {
        try {
            fcmTokenService.registerToken(userId, request.getFcmToken());
            return ApiResponse.success(1000, "FCM 토큰이 등록되었습니다.", null);
        } catch (Exception e) {
            return ApiResponse.fail(4101, "FCM 토큰을 등록하지 못했습니다.");
        }
    }
}