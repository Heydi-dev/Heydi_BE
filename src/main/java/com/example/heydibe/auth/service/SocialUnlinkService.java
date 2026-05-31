package com.example.heydibe.auth.service;

import com.example.heydibe.user.entity.SocialAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialUnlinkService {

    private static final String PROVIDER_GOOGLE = "google";
    private static final String PROVIDER_KAKAO = "kakao";

    private final SocialTokenService socialTokenService;
    private final RestTemplate restTemplate;

    /**
     * DB 삭제 전(트랜잭션 내) 호출해 유효 access token을 확보한다.
     */
    public List<SocialUnlinkTarget> prepareUnlinkTargets(List<SocialAccount> accounts) {
        List<SocialUnlinkTarget> targets = new ArrayList<>();
        for (SocialAccount account : accounts) {
            try {
                String accessToken = socialTokenService.getValidAccessToken(account);
                if (accessToken != null && !accessToken.isBlank()) {
                    targets.add(new SocialUnlinkTarget(account.getProvider(), accessToken));
                }
            } catch (Exception e) {
                log.warn("Failed to prepare OAuth unlink for provider={}", account.getProvider(), e);
            }
        }
        return targets;
    }

    /**
     * commit 이후 best-effort로 provider 연결 해제. 실패해도 탈퇴는 완료된 상태를 유지한다.
     */
    public void unlinkBestEffort(List<SocialUnlinkTarget> targets) {
        for (SocialUnlinkTarget target : targets) {
            try {
                unlinkProvider(target.provider(), target.accessToken());
            } catch (Exception e) {
                log.warn("OAuth unlink failed for provider={}", target.provider(), e);
            }
        }
    }

    private void unlinkProvider(String provider, String accessToken) {
        if (PROVIDER_KAKAO.equals(provider)) {
            unlinkKakao(accessToken);
            return;
        }

        if (PROVIDER_GOOGLE.equals(provider)) {
            revokeGoogle(accessToken);
            return;
        }

        log.warn("Unsupported social provider: {}", provider);
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
            throw new IllegalStateException("Kakao unlink failed: " + response.getStatusCode());
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
            throw new IllegalStateException("Google revoke failed: " + response.getStatusCode());
        }
    }

    public record SocialUnlinkTarget(String provider, String accessToken) {}
}
