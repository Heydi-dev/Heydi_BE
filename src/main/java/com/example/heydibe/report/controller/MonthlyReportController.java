package com.example.heydibe.report.controller;

import com.example.heydibe.common.auth.AuthUser;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.report.dto.MonthlyReportApiDto.AvailableMonthsResult;
import com.example.heydibe.report.dto.MonthlyReportApiDto.MonthlyReportUnifiedResult;
import com.example.heydibe.report.service.MonthlyReportQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports/monthly")
public class MonthlyReportController {

    private final MonthlyReportQueryService service;

    public MonthlyReportController(MonthlyReportQueryService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<AvailableMonthsResult> getAvailableMonths(
            @AuthUser Long userId
    ) {
        return ApiResponse.success(
                "월간 리포트 가능 월 목록 조회에 성공했습니다.",
                service.getAvailableMonths(userId)
        );
    }

    @GetMapping("/{yearMonth}")
    public ApiResponse<MonthlyReportUnifiedResult> getUnified(
            @AuthUser Long userId,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(
                "월간 리포트 조회에 성공했습니다.",
                service.getUnified(userId, yearMonth)
        );
    }
}