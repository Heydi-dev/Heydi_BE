package com.example.heydibe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SessionAuthenticationFilter sessionAuthenticationFilter() {
        return new SessionAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 鍮꾪솢?깊솕 (?몄뀡 湲곕컲 ?몄쬆 ?ъ슜???꾩슂???쒖꽦??媛??
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS ?ㅼ젙 ?곸슜
            .addFilterBefore(sessionAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // ?몄뀡 ?몄쬆 ?꾪꽣 異붽?
            .authorizeHttpRequests(auth -> auth

                // Swagger UI (媛쒕컻 ?섍꼍?? - 異붽? ?꾩슂!
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
    
                // 怨듦컻 API (?몄쬆 遺덊븘??
                .requestMatchers("/files/presigned-url").permitAll() // presignedURL ?앹꽦
                .requestMatchers("/auth/check-username").permitAll() // ?꾩씠??以묐났 ?뺤씤
                .requestMatchers("/user/signup").permitAll() // ?뚯썝媛??
                .requestMatchers("/auth/login").permitAll() // 濡쒓렇??
                
                // ?몄쬆 ?꾩닔 API
                .requestMatchers("/auth/logout").authenticated() // 濡쒓렇?꾩썐
                .requestMatchers("/user/withdraw").authenticated() // ?뚯썝?덊눜
                .requestMatchers("/profile/me").authenticated() // ?꾨줈??議고쉶/?섏젙
                
                // ?섎㉧吏 紐⑤뱺 ?붿껌? ?몄쬆 ?꾩슂
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // ?몄뀡???꾩슂???앹꽦
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // allowCredentials(true)? ?④퍡 ?ъ슜?섎젮硫?setAllowedOriginPatterns ?ъ슜?댁빞 ??
        // React ?꾨줎?몄뿏??origin ?ㅼ젙 (?ㅼ젣 ?꾨줎?몄뿏??二쇱냼濡?蹂寃??꾩슂)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));
        
        // ?덉슜??HTTP 硫붿꽌??
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // ?덉슜???ㅻ뜑
        configuration.setAllowedHeaders(List.of("*"));
        
        // 荑좏궎 諛??몄쬆 ?뺣낫 ?ы븿 ?덉슜 (?몄뀡 荑좏궎 ?꾩넚???꾪빐 ?꾩닔)
        // React?먯꽌 fetch/axios ?ъ슜??credentials: 'include' ?먮뒗 withCredentials: true ?꾩닔
        configuration.setAllowCredentials(true);
        
        // preflight ?붿껌??罹먯떆 ?쒓컙 (珥?
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

