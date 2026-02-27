package com.example.heydibe.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckUsernameResponse {
    private boolean duplicate;

    public static CheckUsernameResponse from(boolean duplicate) {
        return new CheckUsernameResponse(duplicate);
    }
}

