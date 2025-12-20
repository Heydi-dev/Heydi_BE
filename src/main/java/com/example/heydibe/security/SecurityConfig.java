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
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (세션 기반 인증 사용시 필요없지만 성능향상)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 사용
            .addFilterBefore(sessionAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // 세션 인증 필터 추가
            .authorizeHttpRequests(auth -> auth
                // 공개 API (인증 불필요)
                .requestMatchers("/files/presigned-url").permitAll() // presignedURL 생성
                .requestMatchers("/auth/check-username").permitAll() // 아이디 중복 확인
                .requestMatchers("/user/signup").permitAll() // 회원가입
                .requestMatchers("/auth/login").permitAll() // 로그인
                
                // 인증 필요 API
                .requestMatchers("/auth/logout").authenticated() // 로그아웃
                .requestMatchers("/user/withdraw").authenticated() // 회원탈퇴
                .requestMatchers("/profile/me").authenticated() // 프로필 조회/수정
                
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 세션이 필요시 생성
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // allowCredentials(true)와 함께 사용하려면 setAllowedOriginPatterns 사용해야 함
        // React 프론트엔드 origin 설정 (실제 프론트엔드 주소로 변경 필요)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));
        
        // 쿠키 및 인증 정보 포함 허용 (세션 쿠키 전송을 위해 필요)
        // React에서 fetch/axios 사용시 credentials: 'include' 또는 withCredentials: true 필요
        configuration.setAllowCredentials(true);
        
        // preflight 요청의 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
