package com.example.heydibe.security;

import com.example.heydibe.auth.AuthDto;
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
 * ?몄뀡?먯꽌 濡쒓렇???ъ슜???뺣낫瑜??쎌뼱 Spring Security??SecurityContext???ㅼ젙?섎뒗 ?꾪꽣
 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            Object obj = session.getAttribute(LOGIN_USER);
            
            if (obj instanceof AuthDto authDto) {
                // ?몄뀡??濡쒓렇???ъ슜???뺣낫媛 ?덉쑝硫?SecurityContext???ㅼ젙
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authDto.getUserId(), // principal
                    null, // credentials
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

