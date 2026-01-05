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

    public MonthlyReportController(
            MonthlyReportQueryService service,
            AuthUserResolver authUserResolver
    ) {
        this.service = service;
        this.authUserResolver = authUserResolver;
    }

    @PutMapping("/{yearMonth}")
    public ResponseEntity<ApiResponse<MonthlyReportUpsertResult>> upsertMonthlyReport(
            HttpServletRequest request,
            @PathVariable String yearMonth,
            @RequestBody MonthlyReportUpsertRequest body
    ) {
        Long userId = authUserResolver.requireUserId(request);

        MonthlyReportUpsertResult result =
                service.upsertMonthlyReport(userId, yearMonth, body.analysis());

        return ResponseEntity.ok(
                ApiResponse.success(1000, "월간 리포트를 저장했습니다.", result)
        );
    }
}
