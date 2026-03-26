package com.example.heydibe.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LogoutResponse {
    private boolean success;

    public static LogoutResponse from(boolean success) {
        return new LogoutResponse(success);
    }
}
