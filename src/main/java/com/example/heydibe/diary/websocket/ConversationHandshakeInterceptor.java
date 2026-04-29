package com.example.heydibe.diary.websocket;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static com.example.heydibe.security.util.SessionKeys.LOGIN_USER;

@Slf4j
@Component
public class ConversationHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("WS handshake rejected: non-servlet request");
            return false;
        }

        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        String cookieHeader = httpRequest.getHeader("Cookie");
        String origin = httpRequest.getHeader("Origin");
        String host = httpRequest.getHeader("Host");
        String requestedSessionId = httpRequest.getRequestedSessionId();
        boolean hasJSessionCookie = cookieHeader != null && cookieHeader.contains("JSESSIONID=");

        log.info(
                "WS handshake incoming: uri={}, host={}, origin={}, hasCookie={}, hasJSESSIONIDCookie={}, requestedSessionId={}",
                request.getURI(),
                host,
                origin,
                cookieHeader != null,
                hasJSessionCookie,
                requestedSessionId
        );

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn(
                    "WS handshake rejected: session is null, uri={}, host={}, origin={}, cookieHeader={}, requestedSessionId={}",
                    request.getURI(),
                    host,
                    origin,
                    cookieHeader,
                    requestedSessionId
            );
            return false;
        }

        Object rawLoginUser = session.getAttribute(LOGIN_USER);
        if (!(rawLoginUser instanceof Long userId)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn(
                    "WS handshake rejected: LOGIN_USER missing/invalid, uri={}, sessionId={}, rawType={}, rawValue={}",
                    request.getURI(),
                    session.getId(),
                    rawLoginUser == null ? "null" : rawLoginUser.getClass().getName(),
                    rawLoginUser
            );
            return false;
        }

        String diaryIdRaw = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("diaryId");
        if (diaryIdRaw == null || diaryIdRaw.isBlank()) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            log.warn("WS handshake rejected: diaryId query missing, uri={}", request.getURI());
            return false;
        }

        Long diaryId;
        try {
            diaryId = Long.parseLong(diaryIdRaw);
        } catch (NumberFormatException e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            log.warn("WS handshake rejected: diaryId parse failed, raw={}", diaryIdRaw);
            return false;
        }

        attributes.put("userId", userId);
        attributes.put("diaryId", diaryId);
        log.debug("WS handshake accepted: userId={}, diaryId={}, sessionId={}", userId, diaryId, session.getId());
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }
}
