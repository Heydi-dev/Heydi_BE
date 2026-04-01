package com.example.heydibe.settings.service;

import com.example.heydibe.settings.dto.response.ReminderSettingResponse;
import com.example.heydibe.settings.entity.ReminderSetting;
import com.example.heydibe.settings.repository.ReminderSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReminderSettingService {

    private final ReminderSettingRepository reminderSettingRepository;

    @Transactional
    public ReminderSettingResponse getReminderSetting(Long userId) {
        ReminderSetting setting = reminderSettingRepository.findByUserId(userId)
                .orElseGet(() -> reminderSettingRepository.save(createDefault(userId)));

        return ReminderSettingResponse.from(setting);
    }

    @Transactional
    public void disableReminder(Long userId) {
        ReminderSetting setting = reminderSettingRepository.findByUserId(userId)
                .orElseGet(() -> reminderSettingRepository.save(createDefault(userId)));

        setting.disable();
    }

    private ReminderSetting createDefault(Long userId) {
        return ReminderSetting.builder()
                .userId(userId)
                .enabled(true)
                .reminderTime(LocalTime.of(21, 30)) // PM 9:30 기본값
                .build();
    }
}