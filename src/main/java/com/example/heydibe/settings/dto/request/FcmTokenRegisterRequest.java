package com.example.heydibe.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenRegisterRequest {

    @NotBlank(message = "FCM 토큰은 비어 있을 수 없습니다.")
    private String fcmToken;
}