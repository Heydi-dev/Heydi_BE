package com.example.heydibe.user.response;

import com.example.heydibe.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private Long userId;
    private String username;
    private String nickname;

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getUsername(), user.getNickname());
    }
}

