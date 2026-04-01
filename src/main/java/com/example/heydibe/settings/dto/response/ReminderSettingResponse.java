package com.example.heydibe.settings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReminderSettingResponse {

    private boolean enabled;
    private String reminderTime;
    private String daysOfWeek;
}