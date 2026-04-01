package com.example.heydibe.settings.dto.response;

import com.example.heydibe.settings.entity.ReminderSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class ReminderSettingResponse {

    private ReminderDto reminder;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReminderDto {
        private boolean enabled;
        private String meridiem;
        private int hour;
        private int minute;
    }

    public static ReminderSettingResponse from(ReminderSetting setting) {
        LocalTime time = setting.getReminderTime() != null ? setting.getReminderTime() : LocalTime.of(21, 30);

        int hour24 = time.getHour();
        String meridiem = hour24 < 12 ? "AM" : "PM";
        int hour12 = hour24 % 12 == 0 ? 12 : hour24 % 12;

        return ReminderSettingResponse.builder()
                .reminder(
                        ReminderDto.builder()
                                .enabled(setting.isEnabled())
                                .meridiem(meridiem)
                                .hour(hour12)
                                .minute(time.getMinute())
                                .build()
                )
                .build();
    }
}