package com.example.heydibe.settings.controller;

import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.settings.dto.response.ReminderSettingResponse;
import com.example.heydibe.settings.service.ReminderSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings/reminder")
@RequiredArgsConstructor
public class ReminderSettingController {

    private final ReminderSettingService reminderSettingService;

    @GetMapping
    public ApiResponse<ReminderSettingResponse> getReminderSetting(@AuthUser Long userId) {
        try {
            ReminderSettingResponse response = reminderSettingService.getReminderSetting(userId);
            return ApiResponse.success(1000, "알림 설정을 불러왔습니다.", response);
        } catch (Exception e) {
            return ApiResponse.fail(4001, "알림 설정을 불러오지 못했습니다.");
        }
    }

    @PutMapping("/disable")
    public ApiResponse<Void> disableReminder(@AuthUser Long userId) {
        try {
            reminderSettingService.disableReminder(userId);
            return ApiResponse.success(1000, "알림이 비활성화되었습니다.", null);
        } catch (Exception e) {
            return ApiResponse.fail(4102, "알림 비활성화 처리에 실패했습니다.");
        }
    }
}