package com.example.heydibe.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckUsernameResponse {
    private boolean duplicate;

    public static CheckUsernameResponse from(boolean duplicate) {
        return new CheckUsernameResponse(duplicate);
    }

    public String getMessage() {
        return duplicate ? "사용 불가능한 아이디 입니다." : "사용 가능한 아이디 입니다.";
    }
}
