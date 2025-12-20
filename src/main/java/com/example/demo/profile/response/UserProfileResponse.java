package com.example.demo.profile.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private String username;
    private String nickname;
    private String profileImageKey;

    public static UserProfileResponse from(String username, String nickname, String profileImageKey) {
        return new UserProfileResponse(username, nickname, profileImageKey);
    }
}
