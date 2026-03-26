package com.example.heydibe.auth.dto.response;

import com.example.heydibe.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponse {
    private Long userId;
    private String username;
    private String nickname;

    public static SignUpResponse from(User user) {
        return new SignUpResponse(user.getId(), user.getUsername(), user.getNickname());
    }
}
