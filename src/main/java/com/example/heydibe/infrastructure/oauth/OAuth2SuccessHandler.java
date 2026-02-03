package com.example.heydibe.infrastructure.oauth;

import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.example.heydibe.security.util.SessionKeys.LOGIN_USER;

/**
 * OAuth2 인증 성공 후 세션을 설정하고 프론트엔드로 리다이렉트하는 핸들러
 * 인프라 레이어: Spring Security와의 통합 담당
 * 
 * 플로우:
 * 1. OAuth2 인증 성공 → 세션에 userId 저장
 * 2. 프론트엔드로 리다이렉트
 * 3. 프론트엔드에서 /api/auth/me 호출하여 사용자 정보 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.frontend.oauth.success-redirect:/login/success}")
    private String successRedirectPath;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Long userId = (Long) oAuth2User.getAttribute("userId");

            if (userId == null) {
                log.error("OAuth2 인증 성공했지만 userId가 null입니다.");
                redirectToError(response, "USER_ID_NOT_FOUND");
                return;
            }

            User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(() -> {
                        log.error("OAuth2 인증 성공했지만 사용자를 찾을 수 없습니다. userId: {}", userId);
                        return new CustomException(ErrorCode.UNAUTHORIZED);
                    });

            // 세션에 userId 저장
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN_USER, userId);

            log.info("OAuth2 로그인 성공 - userId: {}, username: {}, sessionId: {}", 
                     userId, user.getUsername(), session.getId());

            // 프론트엔드로 리다이렉트 (프론트에서 /api/auth/me 호출하도록)
            String redirectUrl = frontendBaseUrl + successRedirectPath;
            response.sendRedirect(redirectUrl);

        } catch (CustomException e) {
            log.error("OAuth2 인증 실패: {}", e.getErrorCode(), e);
            redirectToError(response, e.getErrorCode().name());
        } catch (Exception e) {
            log.error("OAuth2 인증 처리 중 예상치 못한 오류", e);
            redirectToError(response, "INTERNAL_SERVER_ERROR");
        }
    }

    private void redirectToError(HttpServletResponse response, String errorCode) throws IOException {
        String errorUrl = String.format("%s/login?error=%s", frontendBaseUrl, errorCode);
        response.sendRedirect(errorUrl);
    }
}