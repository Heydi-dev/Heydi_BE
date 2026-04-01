package com.example.heydibe.settings.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.settings.dto.request.ReminderSettingUpdateRequest;
import com.example.heydibe.settings.service.ReminderSettingService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings/reminder")
public class ReminderSettingController {

    private final AuthService authService;
    private final ReminderSettingService reminderSettingService;

    @GetMapping
    public ApiResponse getReminderSetting(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "알림 설정 조회 성공",
                reminderSettingService.getReminderSetting(user.getId())
        );
    }

    @PutMapping
    public ApiResponse updateReminderSetting(
            @Valid @RequestBody ReminderSettingUpdateRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "알림 설정 변경 성공",
                reminderSettingService.updateReminderSetting(user.getId(), request)
        );
    }

    @PutMapping("/disable")
    public ApiResponse disableReminder(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);

        return ApiResponse.success(
                "알림 해제 성공",
                reminderSettingService.disableReminder(user.getId())
        );
    }
}