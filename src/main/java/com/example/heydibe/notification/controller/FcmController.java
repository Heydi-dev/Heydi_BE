package com.example.heydibe.notification.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.notification.dto.request.FcmTestRequest;
import com.example.heydibe.notification.dto.request.FcmTokenRequest;
import com.example.heydibe.notification.service.FcmTokenService;
import com.example.heydibe.user.entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class FcmController {

    private final AuthService authService;
    private final FcmTokenService fcmTokenService;

    @PostMapping("/fcm-token")
    public ApiResponse registerFcmToken(
            @Valid @RequestBody FcmTokenRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        fcmTokenService.registerToken(user.getId(), request);

        return ApiResponse.success("FCM 토큰 등록 성공", null);
    }

    @PostMapping("/test")
    public ApiResponse sendTestNotification(
            @Valid @RequestBody FcmTestRequest request,
            HttpSession session
    ) throws FirebaseMessagingException {
        User user = authService.getLoginUserFromSession(session);
        String messageId = fcmTokenService.sendTestNotification(user.getId(), request);

        return ApiResponse.success("FCM 테스트 발송 성공", messageId);
    }
}