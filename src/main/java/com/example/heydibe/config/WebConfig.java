package com.example.heydibe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 관련 설정
 * 세션 쿠키 설정은 application.properties에서 관리
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Web 관련 설정
}
