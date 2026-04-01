package com.example.heydibe.report.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

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

    // ✅ 캘린더
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyCalendarResult {
        private Long reportId;
        private String reportYearMonth;
        private JsonNode calendar;
    }

    // ✅ 주제
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTopicsResult {
        private Long reportId;
        private String reportYearMonth;
        private JsonNode topics;
    }

    // ✅ 감정
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyEmotionsResult {
        private Long reportId;
        private String reportYearMonth;
        private JsonNode emotions;
    }
}