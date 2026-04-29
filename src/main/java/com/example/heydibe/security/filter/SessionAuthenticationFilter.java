package com.example.heydibe.security.filter;

import com.example.heydibe.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.example.heydibe.security.util.SessionKeys.LOGIN_USER;

/**
 * Reads the logged-in user from HttpSession and populates SecurityContext.
 * Invalid sessions for deleted users are cleared immediately.
 */
@Slf4j
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SessionAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            if (request.getRequestURI().startsWith("/ws/") && request.getRequestedSessionId() != null) {
                log.warn(
                        "SessionAuthenticationFilter found no HttpSession for websocket request. uri={}, requestedSessionId={}, requestedSessionIdValid={}",
                        request.getRequestURI(),
                        request.getRequestedSessionId(),
                        request.isRequestedSessionIdValid()
                );
            }
        } else {
            Object rawLoginUser = session.getAttribute(LOGIN_USER);
            if (rawLoginUser instanceof Long userId) {
                boolean isValidUser = userRepository.findByIdAndDeletedAtIsNull(userId).isPresent();

                if (isValidUser) {
                    // 정상 사용자인 경우만 인증 정보 설정
                    // SecurityContext에 이미 인증 정보가 있으면 덮어쓰지 않음 (OAuth2 등)
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId, // principal
                            null, // credentials
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    // 사용자가 삭제되었거나 존재하지 않는 경우 세션 무효화
                    try {
                        session.invalidate();
                    } catch (Exception ignore) {
                        // 세션 무효화 실패는 무시 (이미 무효화되었을 수 있음)
                    }
                    SecurityContextHolder.clearContext();
                }
            } else if (rawLoginUser != null) {
                log.warn(
                        "SessionAuthenticationFilter skipped invalid LOGIN_USER. uri={}, sessionId={}, rawType={}, rawValue={}",
                        request.getRequestURI(),
                        session.getId(),
                        rawLoginUser.getClass().getName(),
                        rawLoginUser
                );
            }
        }

        filterChain.doFilter(request, response);
    }
}
