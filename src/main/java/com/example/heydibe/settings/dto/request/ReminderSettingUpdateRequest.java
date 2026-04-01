package com.example.heydibe.settings.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReminderSettingUpdateRequest {
    private Boolean enabled;
    private String meridiem;
    private Integer hour;
    private Integer minute;
}