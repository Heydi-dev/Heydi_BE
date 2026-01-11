package com.example.heydibe.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthUserResolver {

    // 🔧 개발용: 무조건 userId = 1 반환
    public Long requireUserId(HttpServletRequest request) {
        return 1L;
    }

    // 🔧 개발용: Authorization 헤더 안 씀
    public static Long requireUserId(String authorization) {
        return 1L;
    }
}
