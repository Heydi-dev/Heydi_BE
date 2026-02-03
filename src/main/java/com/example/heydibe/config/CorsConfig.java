package com.example.heydibe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 설정은 SecurityConfig에서 관리합니다.
 * 중복 설정을 방지하기 위해 이 클래스는 비활성화했습니다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    // CORS 설정은 SecurityConfig.corsConfigurationSource()에서 관리
}
