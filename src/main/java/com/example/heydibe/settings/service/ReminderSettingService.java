package com.example.heydibe.settings.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.settings.dto.request.ReminderSettingUpdateRequest;
import com.example.heydibe.settings.dto.response.ReminderSettingResponse;
import com.example.heydibe.settings.entity.ReminderSetting;
import com.example.heydibe.settings.repository.ReminderSettingRepository;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReminderSettingService {

    private final ReminderSettingRepository reminderSettingRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReminderSettingResponse getReminderSetting(Long userId) {
        ReminderSetting setting = reminderSettingRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSetting(getUser(userId)));

        return toResponse(setting);
    }

    public ReminderSettingResponse updateReminderSetting(Long userId, ReminderSettingUpdateRequest request) {
        ReminderSetting setting = reminderSettingRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSetting(getUser(userId)));

        setting.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        setting.setReminderTime(LocalTime.parse(request.getReminderTime()));
        setting.setDaysOfWeek(request.getDaysOfWeek());

        return toResponse(setting);
    }

    public ReminderSettingResponse disableReminder(Long userId) {
        ReminderSetting setting = reminderSettingRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSetting(getUser(userId)));

        setting.setEnabled(false);

        return toResponse(setting);
    }

    private ReminderSetting createDefaultSetting(User user) {
        ReminderSetting setting = ReminderSetting.builder()
                .user(user)
                .enabled(false)
                .reminderTime(null)
                .daysOfWeek("")
                .build();

        return reminderSettingRepository.save(setting);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private ReminderSettingResponse toResponse(ReminderSetting setting) {
        return ReminderSettingResponse.builder()
                .enabled(setting.isEnabled())
                .reminderTime(setting.getReminderTime() != null ? setting.getReminderTime().toString() : null)
                .daysOfWeek(setting.getDaysOfWeek())
                .build();
    }
}