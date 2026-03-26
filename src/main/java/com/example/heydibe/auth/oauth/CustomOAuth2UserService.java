package com.example.heydibe.auth.oauth;

import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.user.entity.SocialAccount;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.SocialAccountRepository;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Security OAuth2와 통합되는 사용자 서비스
 * OAuth2 인증 플로우에서 사용자 정보를 로드하고 Spring Security 인증 객체로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId();

        OAuthAttributes attr = OAuthAttributes.of(provider, oAuth2User.getAttributes());

        // 사용자 조회 또는 생성
        User user = findOrCreateUser(attr);

        /**
         * 세션에 저장될 인증 객체
         */
        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "userId", user.getId(),
                        "nickname", user.getNickname()
                ),
                "userId"
        );
    }

    /**
     * 소셜 계정 기준으로 사용자 조회 or 생성
     */
    private User findOrCreateUser(OAuthAttributes attr) {
        // 기존 소셜 계정 조회
        return socialAccountRepository.findByProviderAndProviderUserId(attr.getProvider(), attr.getProviderUserId())
                .map(socialAccount -> {
                    // 기존 소셜 계정이 있으면 해당 User 조회
                    return userRepository.findByIdAndDeletedAtIsNull(socialAccount.getUserId())
                            .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
                })
                .orElseGet(() -> {
                    // 기존 소셜 계정이 없으면 새로 생성
                    return createNewUser(attr);
                });
    }

    /**
     * 신규 OAuth 유저 생성 + 소셜 계정 연결
     */
    private User createNewUser(OAuthAttributes attr) {
        // 닉네임 자동 생성 (서버에서 자체적으로 생성)
        String generatedNickname = generateNickname();

        // User 생성
        User user = User.builder()
                .username(attr.getEmail())   // username = email
                .nickname(generatedNickname)  // 서버에서 자동 생성한 닉네임
                .passwordHash(null)  // OAuth2는 비밀번호 없음
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .fcmToken(null)
                .build();

        userRepository.save(user);

        // UserProfile 생성 (프로필 이미지는 null - 프론트엔드에서 기본 이미지 처리)
        UserProfile userProfile = UserProfile.builder()
                .userId(user.getId())
                .profileImageUrl(null)
                .build();

        userProfileRepository.save(userProfile);

        // SocialAccount 생성 (소셜 계정 정보 저장)
        SocialAccount socialAccount = SocialAccount.builder()
                .userId(user.getId())
                .provider(attr.getProvider())
                .providerUserId(attr.getProviderUserId())
                .createdAt(LocalDateTime.now())
                .build();

        socialAccountRepository.save(socialAccount);

        return user;
    }

    /**
     * 서버에서 자동으로 닉네임 생성
     */
    private String generateNickname() {
        // "user" + 랜덤 UUID 앞 8자리로 닉네임 생성
        String randomPart = UUID.randomUUID().toString().substring(0, 8).replace("-", "");
        return "user" + randomPart;
    }
}
