package com.example.heydibe.profile.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {

    @Size(max = 50, message = "nickname length is too long")
    private String nickname;

    @Size(min = 8, max = 255, message = "newPassword must be between 8 and 255 characters")
    private String newPassword;

    @Size(min = 8, max = 255, message = "newPasswordConfirm must be between 8 and 255 characters")
    private String newPasswordConfirm;

    @Size(max = 500, message = "profileImageKey length is too long")
    private String profileImageKey;
}
