package com.example.heydibe.settings.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.settings.dto.request.FcmTokenRequest;
import com.example.heydibe.settings.dto.request.ReminderSettingUpdateRequest;
import com.example.heydibe.settings.service.ReminderSettingService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
public class ReminderSettingController {

    private final AuthService authService;
    private final ReminderSettingService reminderSettingService;

    @GetMapping("/reminder")
    public ApiResponse getReminderSetting(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "알림 설정을 불러왔습니다.",
                reminderSettingService.getReminderSetting(user.getId())
        );
    }

    @PutMapping("/reminder")
    public ApiResponse updateReminderSetting(
            @Valid @RequestBody ReminderSettingUpdateRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        reminderSettingService.updateReminderSetting(user.getId(), request);

        return ApiResponse.success(
                "알림 설정이 저장되었습니다.",
                null
        );
    }

    @PutMapping("/reminder/disable")
    public ApiResponse disableReminder(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);
        reminderSettingService.disableReminder(user.getId());

        return ApiResponse.success(
                "알림이 비활성화되었습니다.",
                null
        );
    }

    @PostMapping("/fcmtoken")
    public ApiResponse registerFcmToken(
            @Valid @RequestBody FcmTokenRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        reminderSettingService.registerFcmToken(user.getId(), request);

        return ApiResponse.success(
                "FCM 토큰이 등록되었습니다.",
                null
        );
    }
}