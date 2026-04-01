package com.example.heydibe.report.controller;

import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.report.service.MonthlyReportQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports/monthly")
public class MonthlyReportController {

    private final MonthlyReportQueryService service;

    public MonthlyReportController(MonthlyReportQueryService service) {
        this.service = service;
    }

    // 월 목록
    @GetMapping
    public ApiResponse getAvailableMonths(@AuthUser Long userId) {
        return ApiResponse.success(
                "월 목록 조회 성공",
                service.getAvailableMonths(userId)
        );
    }

    // 통합
    @GetMapping("/{yearMonth}")
    public ApiResponse getUnified(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "월간 리포트 조회 성공",
                service.getUnified(userId, yearMonth)
        );
    }

    // 캘린더
    @GetMapping("/{yearMonth}/calendar")
    public ApiResponse getCalendar(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "캘린더 조회 성공",
                service.getCalendar(userId, yearMonth)
        );
    }

    // 주제
    @GetMapping("/{yearMonth}/topics")
    public ApiResponse getTopics(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "주제 조회 성공",
                service.getTopics(userId, yearMonth)
        );
    }

    // 감정
    @GetMapping("/{yearMonth}/emotions")
    public ApiResponse getEmotions(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "감정 조회 성공",
                service.getEmotions(userId, yearMonth)
        );
    }
}