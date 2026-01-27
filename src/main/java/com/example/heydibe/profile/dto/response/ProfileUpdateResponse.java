package com.example.heydibe.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileUpdateResponse {
    private Long user_id;
    private String username;
    private String profileImageUrl;
    private String nickname;

    public static ProfileUpdateResponse from(Long userId, String username, String profileImageUrl, String nickname) {
        return new ProfileUpdateResponse(userId, username, profileImageUrl, nickname);
    }
}
