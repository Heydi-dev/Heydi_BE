package com.example.heydibe.settings.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReminderSettingUpdateRequest {

    @NotNull(message = "enabled는 필수입니다.")
    private Boolean enabled;

    @NotBlank(message = "meridiem은 필수입니다.")
    private String meridiem;

    @NotNull(message = "hour는 필수입니다.")
    @Min(value = 1, message = "hour는 1 이상이어야 합니다.")
    @Max(value = 12, message = "hour는 12 이하여야 합니다.")
    private Integer hour;

    @NotNull(message = "minute는 필수입니다.")
    @Min(value = 0, message = "minute는 0 이상이어야 합니다.")
    @Max(value = 59, message = "minute는 59 이하여야 합니다.")
    private Integer minute;
}