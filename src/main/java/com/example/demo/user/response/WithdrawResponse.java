package com.example.demo.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WithdrawResponse {
    private boolean withdrawn;

    public static WithdrawResponse from(boolean withdrawn) {
        return new WithdrawResponse(withdrawn);
    }
}
