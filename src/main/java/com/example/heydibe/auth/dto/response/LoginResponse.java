package com.example.heydibe.auth.dto.response;

import com.example.heydibe.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String fcmToken;

    public static LoginResponse from(User user, String fcmToken) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                fcmToken
        );
    }
}
