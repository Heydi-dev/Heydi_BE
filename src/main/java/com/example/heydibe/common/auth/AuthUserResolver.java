package com.example.heydibe.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import com.example.heydibe.common.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AuthUserResolver {

    // payload에서 "userId":123 또는 "sub":"123" 형태를 찾음 (개발용)
    private static final Pattern USER_ID_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*(\\d+)");
    private static final Pattern SUB_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"?(\\d+)\"?");

    public Long resolveUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;

        String token = auth.substring("Bearer ".length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) return null;

        String payloadJson = decodeBase64Url(parts[1]);
        Long userId = extractLong(USER_ID_PATTERN, payloadJson);
        if (userId != null) return userId;

        return extractLong(SUB_PATTERN, payloadJson);
    }

    private String decodeBase64Url(String s) {
        byte[] decoded = Base64.getUrlDecoder().decode(s);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private Long extractLong(Pattern p, String json) {
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return Long.parseLong(m.group(1));
    }

    public Long requireUserId(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) {
            // 인증 실패 코드/메시지는 너희 규칙에 맞게 바꿔도 됨
            throw new ApiException(4010, "인증이 필요합니다.");
        }
        return userId;
    }
}
