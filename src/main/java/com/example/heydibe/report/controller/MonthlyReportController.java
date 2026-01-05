package com.example.heydibe.report.controller;

import com.example.heydibe.common.api.ApiResponse;
import com.example.heydibe.common.auth.AuthUserResolver;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.service.MonthlyReportQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports/monthly")
public class MonthlyReportController {

    private final MonthlyReportQueryService service;
    private final AuthUserResolver authUserResolver;

    public MonthlyReportController(MonthlyReportQueryService service, AuthUserResolver authUserResolver) {
        this.service = service;
        this.authUserResolver = authUserResolver;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AvailableMonthsResult>> getAvailableMonths(
            HttpServletRequest request
    ) {
        Long userId = authUserResolver.requireUserId(request);
        AvailableMonthsResult result = service.getAvailableMonths(userId);
        return ResponseEntity.ok(ApiResponse.success(1000, "월간 리포트 가능 월 목록 조회에 성공했습니다.", result));
    }

    @GetMapping("/{yearMonth}/reminder")
    public ResponseEntity<ApiResponse<ReminderResult>> getReminder(
            HttpServletRequest request,
            @PathVariable String yearMonth
    ) {
        Long userId = authUserResolver.requireUserId(request);
        ReminderResult result = service.getReminder(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(1000, "지난 달 일기 리마인더를 조회했습니다.", result));
    }

    @GetMapping("/{yearMonth}/calendar")
    public ResponseEntity<ApiResponse<CalendarResult>> getCalendar(
            HttpServletRequest request,
            @PathVariable String yearMonth
    ) {
        Long userId = authUserResolver.requireUserId(request);
        CalendarResult result = service.getCalendar(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(1000, "캘린더 데이터를 조회했습니다.", result));
    }

    @GetMapping("/{yearMonth}/insights")
    public ResponseEntity<ApiResponse<InsightsResult>> getInsights(
            HttpServletRequest request,
            @PathVariable String yearMonth
    ) {
        Long userId = authUserResolver.requireUserId(request);
        InsightsResult result = service.getInsights(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(1000, "월간 인사이트 리포트 조회에 성공했습니다.", result));
    }

    @GetMapping("/{yearMonth}/activities")
    public ResponseEntity<ApiResponse<ActivitiesResult>> getActivities(
            HttpServletRequest request,
            @PathVariable String yearMonth
    ) {
        Long userId = authUserResolver.requireUserId(request);
        ActivitiesResult result = service.getActivities(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(1000, "월간 활동 리포트 조회에 성공했습니다.", result));
    }

    @GetMapping("/{yearMonth}/preferences")
    public ResponseEntity<ApiResponse<PreferencesResult>> getPreferences(
            HttpServletRequest request,
            @PathVariable String yearMonth
    ) {
        Long userId = authUserResolver.requireUserId(request);
        PreferencesResult result = service.getPreferences(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(1000, "좋아하는 것/싫어하는 것 리포트 조회에 성공했습니다.", result));
    }

    @GetMapping("/{yearMonth}/topics")
    public ResponseEntity<ApiResponse<TopicsResult>> getTopics(
            HttpServletRequest request,
            @PathVariable String yearMonth
    ) {
        Long userId = authUserResolver.requireUserId(request);
        TopicsResult result = service.getTopics(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(1000, "월간 주요 주제 리포트 조회에 성공했습니다.", result));
    }

    @GetMapping("/{yearMonth}/emotions")
    public ResponseEntity<ApiResponse<EmotionsResult>> getEmotions(
            HttpServletRequest request,
            @PathVariable String yearMonth
    ) {
        Long userId = authUserResolver.requireUserId(request);
        EmotionsResult result = service.getEmotions(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(1000, "월간 감정 변화 리포트 조회에 성공했습니다.", result));
    }
}
