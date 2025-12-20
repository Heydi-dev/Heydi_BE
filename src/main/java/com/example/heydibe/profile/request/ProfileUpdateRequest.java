package com.example.heydibe.profile.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {

    @Size(max = 50, message = "nickname 길이가 ?�무 깁니??")
    private String nickname;

    @Size(min = 8, max = 255, message = "newPassword??8~255?�여???�니??")
    private String newPassword;

    @Size(min = 8, max = 255, message = "newPasswordConfirm??8~255?�여???�니??")
    private String newPasswordConfirm;

    @Size(max = 500, message = "profileImageKey 길이가 ?�무 깁니??")
    private String profileImageKey;
}

