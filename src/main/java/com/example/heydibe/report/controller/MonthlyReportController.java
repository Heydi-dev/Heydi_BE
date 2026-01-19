package com.example.heydibe.report.controller;

import com.example.heydibe.common.api.ApiResponse;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.service.MonthlyReportQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports/monthly")
public class MonthlyReportController {

    private final MonthlyReportQueryService monthlyReportQueryService;

    public MonthlyReportController(MonthlyReportQueryService monthlyReportQueryService) {
        this.monthlyReportQueryService = monthlyReportQueryService;
    }

    // 개발용: 로그인/토큰 없을 때 userId=1 고정
    private Long devUserId() {
        return 1L;
    }

    @GetMapping
    public ApiResponse<AvailableMonthsResult> getAvailableMonths() {
        Long userId = devUserId();
        AvailableMonthsResult result = monthlyReportQueryService.getAvailableMonths(userId);
        return ApiResponse.success(1000, "월간 리포트 가능 월 목록 조회에 성공했습니다.", result);
    }

    @GetMapping("/{yearMonth}/topics")
    public ApiResponse<TopicsResult> getTopics(@PathVariable String yearMonth) {
        Long userId = devUserId();
        TopicsResult result = monthlyReportQueryService.getTopics(userId, yearMonth);
        return ApiResponse.success(1000, "월간 주요 주제 리포트 조회에 성공했습니다.", result);
    }

    @GetMapping("/{yearMonth}/preferences")
    public ApiResponse<PreferencesResult> getPreferences(@PathVariable String yearMonth) {
        Long userId = devUserId();
        PreferencesResult result = monthlyReportQueryService.getPreferences(userId, yearMonth);
        return ApiResponse.success(1000, "좋아하는 것/싫어하는 것 리포트 조회에 성공했습니다.", result);
    }

    @GetMapping("/{yearMonth}/activities")
    public ApiResponse<ActivitiesResult> getActivities(@PathVariable String yearMonth) {
        Long userId = devUserId();
        ActivitiesResult result = monthlyReportQueryService.getActivities(userId, yearMonth);
        return ApiResponse.success(1000, "월간 활동 리포트 조회에 성공했습니다.", result);
    }

    @GetMapping("/{yearMonth}/insights")
    public ApiResponse<InsightsResult> getInsights(@PathVariable String yearMonth) {
        Long userId = devUserId();
        InsightsResult result = monthlyReportQueryService.getInsights(userId, yearMonth);
        return ApiResponse.success(1000, "월간 인사이트 리포트 조회에 성공했습니다.", result);
    }

    @GetMapping("/{yearMonth}/calendar")
    public ApiResponse<CalendarResult> getCalendar(@PathVariable String yearMonth) {
        Long userId = devUserId();
        CalendarResult result = monthlyReportQueryService.getCalendar(userId, yearMonth);
        return ApiResponse.success(1000, "캘린더 데이터를 조회했습니다.", result);
    }

    @GetMapping("/{yearMonth}/reminder")
    public ApiResponse<ReminderResult> getReminder(@PathVariable String yearMonth) {
        Long userId = devUserId();
        ReminderResult result = monthlyReportQueryService.getReminder(userId, yearMonth);
        return ApiResponse.success(1000, "지난 달 일기 리마인더를 조회했습니다.", result);
    }
}
