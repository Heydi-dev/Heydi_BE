package com.example.heydibe.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReminderSettingUpdateRequest {

    private Boolean enabled;

    @NotBlank(message = "reminderTime은 필수입니다.")
    private String reminderTime;

    @NotBlank(message = "daysOfWeek는 필수입니다.")
    private String daysOfWeek;
}