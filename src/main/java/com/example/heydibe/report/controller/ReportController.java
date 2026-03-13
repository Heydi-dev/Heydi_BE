package com.example.heydibe.report.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.report.dto.request.MonthlyReportEntryRequest;
import com.example.heydibe.report.dto.response.MonthlyReportEntryResponse;
import com.example.heydibe.report.service.MonthlyReportEntryService;
import com.example.heydibe.user.entity.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final AuthService authService;
    private final MonthlyReportEntryService monthlyReportEntryService;

    @GetMapping("/monthly")
    public String getAvailableMonths() {
        return "get available months - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/emotions")
    public String getMonthlyEmotions(@PathVariable String yearMonth) {
        return "get monthly emotions - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/topics")
    public String getMonthlyTopics(@PathVariable String yearMonth) {
        return "get monthly topics - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/preferences")
    public String getMonthlyPreferences(@PathVariable String yearMonth) {
        return "get monthly preferences - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/activities")
    public String getMonthlyActivities(@PathVariable String yearMonth) {
        return "get monthly activities - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/insights")
    public String getMonthlyInsights(@PathVariable String yearMonth) {
        return "get monthly insights - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/calendar")
    public String getMonthlyCalendar(@PathVariable String yearMonth) {
        return "get monthly calendar - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/reminder")
    public String getMonthlyReminder(@PathVariable String yearMonth) {
        return "get monthly reminder - TODO";
    }

    @PostMapping("/monthly/{yearMonth}/entries")
    public ApiResponse<MonthlyReportEntryResponse> includeDiaryInMonthlyReport(
            @PathVariable String yearMonth,
            @RequestBody MonthlyReportEntryRequest request,
            HttpSession session) {
        User user = authService.getLoginUserFromSession(session);
        MonthlyReportEntryResponse response = monthlyReportEntryService.includeDiaryInMonthlyReport(
                user.getId(),
                yearMonth,
                request.getDiaryId());
        return ApiResponse.success("리포트로 전송 완료", response);
    }
}

