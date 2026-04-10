package com.example.heydibe.report.controller;

import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.report.service.MonthlyReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports/monthly")
@RequiredArgsConstructor
public class MonthlyReportController {

    private final MonthlyReportQueryService service;

    @GetMapping
    public ApiResponse getAvailableMonths(@AuthUser Long userId) {
        return ApiResponse.success(
                "월간 리포트 가능 월 목록 조회에 성공했습니다.",
                service.getAvailableMonths(userId)
        );
    }

    @GetMapping("/{yearMonth}")
    public ApiResponse getMonthlyReport(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "월간 리포트 조회에 성공했습니다.",
                service.getMonthlyReport(userId, yearMonth)
        );
    }

    @GetMapping("/{yearMonth}/calendar")
    public ApiResponse getCalendar(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "캘린더 데이터를 조회했습니다.",
                service.getCalendar(userId, yearMonth)
        );
    }

    @GetMapping("/{yearMonth}/topics")
    public ApiResponse getTopics(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "월간 주요 주제 리포트 조회에 성공했습니다.",
                service.getTopics(userId, yearMonth)
        );
    }

    @GetMapping("/{yearMonth}/emotions")
    public ApiResponse getEmotions(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "월간 감정 변화 리포트 조회에 성공했습니다.",
                service.getEmotions(userId, yearMonth)
        );
    }
}