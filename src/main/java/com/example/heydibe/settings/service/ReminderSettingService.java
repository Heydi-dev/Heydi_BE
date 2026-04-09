package com.example.heydibe.settings.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.settings.dto.request.FcmTokenRequest;
import com.example.heydibe.settings.dto.request.ReminderSettingUpdateRequest;
import com.example.heydibe.settings.dto.response.ReminderSettingResponse;
import com.example.heydibe.settings.entity.ReminderSetting;
import com.example.heydibe.settings.repository.ReminderSettingRepository;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReminderSettingService {

    private final ReminderSettingRepository reminderSettingRepository;
    private final UserRepository userRepository;

    public ReminderSettingResponse getReminderSetting(Long userId) {
        ReminderSetting setting = reminderSettingRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSetting(getUser(userId)));

        return toResponse(setting);
    }

    public void updateReminderSetting(Long userId, ReminderSettingUpdateRequest request) {
        validateMeridiem(request.getMeridiem());

        ReminderSetting setting = reminderSettingRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSetting(getUser(userId)));

        setting.setEnabled(request.getEnabled());
        setting.setMeridiem(request.getMeridiem().toUpperCase());
        setting.setHour(request.getHour());
        setting.setMinute(request.getMinute());
    }

    public void disableReminder(Long userId) {
        ReminderSetting setting = reminderSettingRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSetting(getUser(userId)));

        setting.setEnabled(false);
    }

    public void registerFcmToken(Long userId, FcmTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        user.updateFcmToken(request.getFcmToken());
    }

    private ReminderSetting createDefaultSetting(User user) {
        ReminderSetting setting = ReminderSetting.builder()
                .user(user)
                .enabled(false)
                .meridiem("AM")
                .hour(9)
                .minute(0)
                .build();

        return reminderSettingRepository.save(setting);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private ReminderSettingResponse toResponse(ReminderSetting setting) {
        return ReminderSettingResponse.builder()
                .reminder(ReminderSettingResponse.Reminder.builder()
                        .enabled(setting.isEnabled())
                        .meridiem(setting.getMeridiem())
                        .hour(setting.getHour())
                        .minute(setting.getMinute())
                        .build())
                .build();
    }

    private void validateMeridiem(String meridiem) {
        if (!"AM".equalsIgnoreCase(meridiem) && !"PM".equalsIgnoreCase(meridiem)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}