
package com.example.heydibe.infrastructure.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

/**
 * OAuth2 인증 실패 시 프론트엔드로 리다이렉트하는 핸들러
 */
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.frontend.oauth.failure-redirect:/login/failure}")
    private String failureRedirectPath;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        URI baseUri = URI.create(frontendBaseUrl);

        String redirectUrl = UriComponentsBuilder.newInstance()
                .scheme(baseUri.getScheme())
                .host(baseUri.getHost())
                .port(baseUri.getPort())
                .path(failureRedirectPath)
                .queryParam("error", "oauth_authentication_failed")
                .queryParam("message", exception.getMessage() != null ? exception.getMessage() : "OAuth 인증에 실패했습니다.")
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
