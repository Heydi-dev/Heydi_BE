package com.example.heydibe.report.controller;

import com.example.heydibe.common.api.ApiResponse;
import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.service.MonthlyReportQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports/monthly")
public class MonthlyReportController {

    private final MonthlyReportQueryService service;

    public MonthlyReportController(MonthlyReportQueryService service) {
        this.service = service;
    }

    // 1) GET /reports/monthly
    @GetMapping
    public ApiResponse<AvailableMonthsResult> getAvailableMonths(@AuthUser Long userId) {
        return ApiResponse.success(
                1000,
                "월간 리포트 가능 월 목록 조회에 성공했습니다.",
                service.getAvailableMonths(userId)
        );
    }

    // ✅ (통합) GET /reports/monthly/{yearMonth}
    @GetMapping("/{yearMonth}")
    public ApiResponse<MonthlyReportUnifiedResult> getUnified(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                1000,
                "월간 리포트 조회에 성공했습니다.",
                service.getUnified(userId, yearMonth)
        );
    }

    // 2) GET /reports/monthly/{yearMonth}/topics  (수정된 명세)
    @GetMapping("/{yearMonth}/topics")
    public ApiResponse<TopicsResult> getTopics(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                1000,
                "월간 주요 주제 리포트 조회에 성공했습니다.",
                service.getTopics(userId, yearMonth)
        );
    }

    // 3) GET /reports/monthly/{yearMonth}/calendar  (entries만)
    @GetMapping("/{yearMonth}/calendar")
    public ApiResponse<CalendarResult> getCalendar(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                1000,
                "캘린더 데이터를 조회했습니다.",
                service.getCalendar(userId, yearMonth)
        );
    }
}