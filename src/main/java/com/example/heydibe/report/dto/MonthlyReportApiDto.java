package com.example.heydibe.report.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MonthlyReportApiDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableMonthsResult {
        private List<String> availableMonths;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyReportUnifiedResult {
        private Long reportId;
        private Long userId;
        private String reportYearMonth;
        private JsonNode analysisJson;
        private LocalDateTime createdAt;
    }
}