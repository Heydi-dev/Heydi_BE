package com.example.heydibe.settings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReminderSettingResponse {

    private Reminder reminder;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Reminder {
        private boolean enabled;
        private String meridiem;
        private Integer hour;
        private Integer minute;
    }
}