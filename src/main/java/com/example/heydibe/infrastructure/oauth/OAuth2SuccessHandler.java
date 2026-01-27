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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.example.heydibe.security.util.SessionKeys.LOGIN_USER;

/**
 * OAuth2 인증 성공 후 세션을 설정하고 프론트엔드로 리다이렉트하는 핸들러
 * 인프라 레이어: Spring Security와의 통합 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Value("${app.frontend.base-url:http://localhost:3000}")
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

            // CustomOAuth2UserService에서 설정한 userId 속성 가져오기
            Long userId = (Long) oAuth2User.getAttribute("userId");

            if (userId == null) {
                log.error("OAuth2 인증 성공했지만 userId가 null입니다.");
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            // DB에서 사용자 정보 조회
            User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(() -> {
                        log.error("OAuth2 인증 성공했지만 사용자를 찾을 수 없습니다. userId: {}", userId);
                        return new CustomException(ErrorCode.UNAUTHORIZED);
                    });

            // 세션에 userId 저장
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN_USER, userId);

            log.info("OAuth2 로그인 성공 - userId: {}, username: {}", userId, user.getUsername());

            // 프론트엔드로 리다이렉트 (사용자 정보를 쿼리 파라미터로 전달)
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendBaseUrl + successRedirectPath)
                    .queryParam("userId", userId)
                    .queryParam("username", URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8))
                    .queryParam("nickname", URLEncoder.encode(user.getNickname(), StandardCharsets.UTF_8))
                    .toUriString();

            log.info("OAuth2 리다이렉트 URL: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생", e);
            throw e;
        }
    }
}
