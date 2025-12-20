package com.example.heydibe.security;

import com.example.heydibe.auth.AuthDto;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.example.heydibe.auth.session.SessionKeys.LOGIN_USER;

/**
 * 세션에서 로그인한 사용자 정보를 읽어 Spring Security의 SecurityContext에 설정하는 필터
 * 삭제된 사용자의 세션을 무효화 처리
 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SessionAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // SecurityContext 초기화(이전 인증 정보 제거)
        SecurityContextHolder.clearContext();
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            Object obj = session.getAttribute(LOGIN_USER);
            
            if (obj instanceof AuthDto authDto) {
                // 세션에 저장된 사용자가 실제로 존재하고 삭제되지 않았는지 DB에서 확인
                boolean isValidUser = userRepository.findByIdAndDeletedAtIsNull(authDto.getUserId()).isPresent();
                
                if (isValidUser) {
                    // 정상 사용자인 경우만 인증 정보 설정
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        authDto.getUserId(), // principal
                        null, // credentials
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 사용자가 삭제되었거나 존재하지 않는 경우 세션 무효화
                    try {
                        session.invalidate();
                    } catch (Exception ignore) {
                        // 세션 무효화 실패는 무시 (이미 무효화되었을 수 있음)
                    }
                    SecurityContextHolder.clearContext();
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
