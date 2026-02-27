package com.example.heydibe.auth.response;

import com.example.heydibe.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String username;
    private String nickname;

    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname()
        );
    }
}

