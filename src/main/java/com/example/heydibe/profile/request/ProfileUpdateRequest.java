package com.example.heydibe.profile.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {

    @Size(max = 50, message = "nickname 길이가 너무 깁니다.")
    private String nickname;

    @Size(min = 8, max = 255, message = "newPassword는 8~255자여야 합니다.")
    private String newPassword;

    @Size(min = 8, max = 255, message = "newPasswordConfirm는 8~255자여야 합니다.")
    private String newPasswordConfirm;

    @Size(max = 500, message = "profileImageKey 길이가 너무 깁니다.")
    private String profileImageKey;
}
