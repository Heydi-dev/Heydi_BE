package com.example.heydibe.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthDto {
    private final Long userId;     // principal
    private final String username;
    private final String nickname;
}

