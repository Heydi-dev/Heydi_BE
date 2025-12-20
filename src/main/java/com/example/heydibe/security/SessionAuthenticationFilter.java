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
 * ?¸ì…˜?ì„œ ë¡œê·¸???¬ìš©???•ë³´ë¥??½ì–´ Spring Security??SecurityContext???¤ì •?˜ëŠ” ?„í„°
 * ?? œ???¬ìš©?ì˜ ?¸ì…˜?€ ë¬´íš¨??ì²˜ë¦¬
 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SessionAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // SecurityContext ì´ˆê¸°??(?´ì „ ?¸ì¦ ?•ë³´ ?œê±°)
        SecurityContextHolder.clearContext();
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            Object obj = session.getAttribute(LOGIN_USER);
            
            if (obj instanceof AuthDto authDto) {
                // ?¸ì…˜???€?¥ëœ ?¬ìš©?ê? ?¤ì œë¡?ì¡´ì¬?˜ê³  ?? œ?˜ì? ?Šì•˜?”ì? DB?ì„œ ?•ì¸
                boolean isValidUser = userRepository.findByIdAndDeletedAtIsNull(authDto.getUserId()).isPresent();
                
                if (isValidUser) {
                    // ?•ìƒ ?¬ìš©?ì¸ ê²½ìš°?ë§Œ ?¸ì¦ ?•ë³´ ?¤ì •
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        authDto.getUserId(), // principal
                        null, // credentials
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // ?¬ìš©?ê? ?? œ?˜ì—ˆê±°ë‚˜ ì¡´ì¬?˜ì? ?ŠëŠ” ê²½ìš° ?¸ì…˜ ë¬´íš¨??
                    try {
                        session.invalidate();
                    } catch (Exception ignore) {
                        // ?¸ì…˜ ë¬´íš¨???¤íŒ¨??ë¬´ì‹œ (?´ë? ë¬´íš¨?”ë˜?ˆì„ ???ˆìŒ)
                    }
                    SecurityContextHolder.clearContext();
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}


