package com.example.heydibe.auth.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.user.entity.SocialAccount;
import com.example.heydibe.user.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialUnlinkService {

    private static final String PROVIDER_GOOGLE = "google";
    private static final String PROVIDER_KAKAO = "kakao";

    private final SocialAccountRepository socialAccountRepository;
    private final SocialTokenService socialTokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    public void unlinkAllByUserId(Long userId) {
        List<SocialAccount> accounts = socialAccountRepository.findByUserId(userId);
        for (SocialAccount account : accounts) {
            unlinkAccount(account);
        }
    }

    private void unlinkAccount(SocialAccount account) {
        String accessToken = socialTokenService.getValidAccessToken(account);
        if (accessToken == null || accessToken.isBlank()) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        if (PROVIDER_KAKAO.equals(account.getProvider())) {
            unlinkKakao(accessToken);
            return;
        }

        if (PROVIDER_GOOGLE.equals(account.getProvider())) {
            revokeGoogle(accessToken);
            return;
        }

        log.warn("Unsupported social provider: {}", account.getProvider());
    }

    private void unlinkKakao(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v1/user/unlink",
                HttpMethod.POST,
                entity,
                String.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    private void revokeGoogle(String accessToken) {
        String url = "https://oauth2.googleapis.com/revoke?token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }
}
