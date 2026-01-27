package com.example.heydibe.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {
    private Long user_id;
    private String username;
    private String profileImageUrl;
    private String nickname;
    private String password;

    public static ProfileResponse from(Long userId, String username, String profileImageUrl, String nickname, String passwordHash) {
        return new ProfileResponse(userId, username, profileImageUrl, nickname, passwordHash);
    }
}
