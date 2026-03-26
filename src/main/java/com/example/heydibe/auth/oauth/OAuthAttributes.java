package com.example.heydibe.auth.oauth;

import lombok.Getter;
import java.util.Map;

@Getter
public class OAuthAttributes {

    private final String provider;
    private final String providerUserId;
    private final String email;
    private final String nickname;
    private final Map<String, Object> attributes;

    private OAuthAttributes(
            String provider,
            String providerUserId,
            String email,
            String nickname,
            Map<String, Object> attributes
    ) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.nickname = nickname;
        this.attributes = attributes;
    }

    public static OAuthAttributes of(
            String provider,
            Map<String, Object> attributes
    ) {
        if ("kakao".equals(provider)) {
            return ofKakao(attributes);
        }
        return ofGoogle(attributes);
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attr) {
        return new OAuthAttributes(
                "google",
                (String) attr.get("sub"),
                (String) attr.get("email"),
                (String) attr.get("name"),
                attr
        );
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attr) {

        @SuppressWarnings("unchecked")
        Map<String, Object> account =
                (Map<String, Object>) attr.get("kakao_account");
        @SuppressWarnings("unchecked")
        Map<String, Object> profile =
                (Map<String, Object>) account.get("profile");

        return new OAuthAttributes(
                "kakao",
                String.valueOf(attr.get("id")),
                (String) account.get("email"),
                (String) profile.get("nickname"),
                attr
        );
    }
}
