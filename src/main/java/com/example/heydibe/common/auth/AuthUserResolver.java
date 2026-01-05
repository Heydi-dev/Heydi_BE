package com.example.heydibe.common.auth;

import com.example.heydibe.common.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthUserResolver {

    private final boolean allowTestHeader;

    public AuthUserResolver(
            @Value("${auth.allow-test-header:true}") boolean allowTestHeader
    ) {
        this.allowTestHeader = allowTestHeader;
    }

    public Long requireUserId(HttpServletRequest request) {

        // ✅ 로컬 테스트용
        if (allowTestHeader) {
            String testUserId = request.getHeader("X-USER-ID");
            if (testUserId != null) {
                return Long.parseLong(testUserId);
            }
        }

        // ❌ 실제 토큰 인증 (아직 없음)
        throw new ApiException(4010, "인증이 필요합니다.");
    }
}
