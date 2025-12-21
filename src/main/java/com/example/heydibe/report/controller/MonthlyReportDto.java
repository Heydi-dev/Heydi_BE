package com.example.heydibe.report.controller;

import java.time.LocalDateTime;

public class MonthlyReportDto {
    public record UpsertRequest(String analysisJson) {}
    public record Response(Long reportId, Long userId, String yearMonth, String analysisJson, LocalDateTime createdAt) {}
}
