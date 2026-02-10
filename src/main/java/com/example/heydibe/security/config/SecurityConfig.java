package com.example.heydibe.security.config;

import com.example.heydibe.user.repository.UserRepository;
import com.example.heydibe.auth.oauth.CustomOAuth2UserService;
import com.example.heydibe.infrastructure.oauth.OAuth2SuccessHandler;
import com.example.heydibe.infrastructure.oauth.OAuth2FailureHandler;
import com.example.heydibe.security.filter.SessionAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    // 비밀번호 해싱/매칭에 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 세션 기반 인증 필터
     * - 요청마다 HttpSession에서 로그인 사용자 확인
     * - 있으면 SecurityContext에 Authentication 세팅
     */
    @Bean
    public SessionAuthenticationFilter sessionAuthenticationFilter() {
        return new SessionAuthenticationFilter(userRepository);
    }

    /**
     * Spring Security 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (세션 기반 + REST API)
            .csrf(csrf -> csrf.disable())

            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // formLogin, httpBasic 비활성화 (REST API이므로 HTML 로그인 페이지 불필요)
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable())

            // REST API용 JSON 응답 설정
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    
                    // ApiResponse 형식으로 JSON 응답
                    String jsonResponse = "{\"success\":false,\"code\":\"401\",\"message\":\"인증이 필요합니다.\",\"result\":null}";
                    response.getWriter().write(jsonResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    
                    // ApiResponse 형식으로 JSON 응답
                    String jsonResponse = "{\"success\":false,\"code\":\"403\",\"message\":\"접근 권한이 없습니다.\",\"result\":null}";
                    response.getWriter().write(jsonResponse);
                })
            )

            // 세션 인증 필터 등록
            .addFilterBefore(
                sessionAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class
            )

            // 요청별 권한 설정
            .authorizeHttpRequests(auth -> auth
                // ===== 공개 API =====
                .requestMatchers(
                    "/auth/login",
                    "/auth/signup",
                    "/auth/check-username",
                    "/oauth2/**",           // Spring Security OAuth2 자동 플로우 (/oauth2/authorization/{provider})
                    "/login/**",            // OAuth2 관련 경로 전체 허용
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // ===== 인증 필요 API =====
                .requestMatchers(
                    "/auth/logout",
                    "/auth/withdraw",
                    "/mypage/profile/**"
                ).authenticated()

                .anyRequest().authenticated()
            )

            // 세션 정책
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // ===== OAuth2 로그인 설정 =====
            .oauth2Login(oauth2 -> oauth2
                // OAuth2 인증 엔드포인트 명시 (기본 로그인 페이지 방지)
                .authorizationEndpoint(auth -> auth
                    .baseUri("/oauth2/authorization")
                )
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
            );

        return http.build();
    }

    /**
     * CORS 설정
     * allowCredentials(true)와 함께 사용할 때는 와일드카드 패턴이 제대로 작동해야 함
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 포트 변경에 대응하기 위해 와일드카드 패턴 사용
        // allowCredentials(true)와 함께 사용 가능
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Cookie")); // 세션 쿠키 노출
        configuration.setAllowCredentials(true); // 세션 쿠키 전송을 위해 필수
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}