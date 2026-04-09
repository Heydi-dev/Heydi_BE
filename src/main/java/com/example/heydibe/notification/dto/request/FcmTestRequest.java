package com.example.heydibe.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTestRequest {

    @NotBlank(message = "title은 필수입니다.")
    private String title;

    @NotBlank(message = "body는 필수입니다.")
    private String body;
}