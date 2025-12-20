package com.example.heydibe.security;

import com.example.heydibe.user.repository.UserRepository;
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

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SessionAuthenticationFilter sessionAuthenticationFilter() {
        return new SessionAuthenticationFilter(userRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF ë¹„í™œ?±í™” (?¸ì…˜ ê¸°ë°˜ ?¸ì¦ ?¬ìš©???„ìš”???œì„±??ê°€??
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS ?¤ì • ?ìš©
            .addFilterBefore(sessionAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // ?¸ì…˜ ?¸ì¦ ?„í„° ì¶”ê?
            .authorizeHttpRequests(auth -> auth
                // ê³µê°œ API (?¸ì¦ ë¶ˆí•„??
                .requestMatchers("/files/presigned-url").permitAll() // presignedURL ?ì„±
                .requestMatchers("/auth/check-username").permitAll() // ?„ì´??ì¤‘ë³µ ?•ì¸
                .requestMatchers("/user/signup").permitAll() // ?Œì›ê°€??
                .requestMatchers("/auth/login").permitAll() // ë¡œê·¸??
                
                // ?¸ì¦ ?„ìˆ˜ API
                .requestMatchers("/auth/logout").authenticated() // ë¡œê·¸?„ì›ƒ
                .requestMatchers("/user/withdraw").authenticated() // ?Œì›?ˆí‡´
                .requestMatchers("/profile/me").authenticated() // ?„ë¡œ??ì¡°íšŒ/?˜ì •
                
                // ?˜ë¨¸ì§€ ëª¨ë“  ?”ì²­?€ ?¸ì¦ ?„ìš”
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // ?¸ì…˜???„ìš”???ì„±
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // allowCredentials(true)?€ ?¨ê»˜ ?¬ìš©?˜ë ¤ë©?setAllowedOriginPatterns ?¬ìš©?´ì•¼ ??
        // React ?„ë¡ ?¸ì—”??origin ?¤ì • (?¤ì œ ?„ë¡ ?¸ì—”??ì£¼ì†Œë¡?ë³€ê²??„ìš”)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));
        
        // ?ˆìš©??HTTP ë©”ì„œ??
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // ?ˆìš©???¤ë”
        configuration.setAllowedHeaders(List.of("*"));
        
        // ì¿ í‚¤ ë°??¸ì¦ ?•ë³´ ?¬í•¨ ?ˆìš© (?¸ì…˜ ì¿ í‚¤ ?„ì†¡???„í•´ ?„ìˆ˜)
        // React?ì„œ fetch/axios ?¬ìš©??credentials: 'include' ?ëŠ” withCredentials: true ?„ìˆ˜
        configuration.setAllowCredentials(true);
        
        // preflight ?”ì²­??ìºì‹œ ?œê°„ (ì´?
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}


