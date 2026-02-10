package com.example.heydibe.auth.service;

import com.example.heydibe.auth.dto.OAuthTokenResponse;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.user.entity.SocialAccount;
import com.example.heydibe.user.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialTokenService {

    private static final int EXPIRY_SAFETY_SECONDS = 30;

    private final SocialAccountRepository socialAccountRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void saveTokens(Long userId, String provider, String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        socialAccountRepository.findByUserIdAndProvider(userId, provider)
                .ifPresentOrElse(
                        account -> {
                            account.updateTokens(accessToken, refreshToken, tokenExpiresAt);
                            socialAccountRepository.save(account);
                        },
                        () -> log.warn("SocialAccount not found for userId: {}, provider: {}", userId, provider)
                );
    }

    @Transactional
    public String getValidAccessToken(SocialAccount account) {
        if (account.getAccessToken() == null || account.getAccessToken().isBlank()) {
            return null;
        }

        if (!needsRefresh(account)) {
            return account.getAccessToken();
        }

        if (account.getRefreshToken() == null || account.getRefreshToken().isBlank()) {
            return null;
        }

        TokenRefreshResult refreshResult = refreshAccessToken(account.getProvider(), account.getRefreshToken());
        account.updateTokens(refreshResult.accessToken(), refreshResult.refreshToken(), refreshResult.expiresAt());
        socialAccountRepository.save(account);
        return refreshResult.accessToken();
    }

    private boolean needsRefresh(SocialAccount account) {
        if (account.getTokenExpiresAt() == null) {
            return false;
        }
        return account.getTokenExpiresAt().isBefore(LocalDateTime.now().plusSeconds(EXPIRY_SAFETY_SECONDS));
    }

    private TokenRefreshResult refreshAccessToken(String provider, String refreshToken) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);
        if (registration == null) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", registration.getClientId());
        if (registration.getClientSecret() != null) {
            params.add("client_secret", registration.getClientSecret());
        }
        params.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<OAuthTokenResponse> response = restTemplate.postForEntity(
                registration.getProviderDetails().getTokenUri(),
                entity,
                OAuthTokenResponse.class
        );

        OAuthTokenResponse body = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful() || body == null) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        String newAccessToken = body.getAccessToken();
        String newRefreshToken = body.getRefreshToken() != null ? body.getRefreshToken() : refreshToken;
        Integer expiresIn = body.getExpiresIn();

        LocalDateTime expiresAt = expiresIn != null
                ? LocalDateTime.now().plusSeconds(expiresIn)
                : null;

        return new TokenRefreshResult(newAccessToken, newRefreshToken, expiresAt);
    }

    private record TokenRefreshResult(String accessToken, String refreshToken, LocalDateTime expiresAt) {}
}
