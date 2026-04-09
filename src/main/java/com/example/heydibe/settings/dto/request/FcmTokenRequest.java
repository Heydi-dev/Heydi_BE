package com.example.heydibe.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenRequest {

    @NotBlank(message = "fcmToken은 필수입니다.")
    private String fcmToken;
}