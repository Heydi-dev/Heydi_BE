package com.example.heydibe.report.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyReportQueryService {

    private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

    private final MonthlyReportRepository monthlyReportRepository;

    // 월 목록 조회
    public AvailableMonthsResult getAvailableMonths(Long userId) {
        validateUserId(userId);

        List<MonthlyReport> reports = monthlyReportRepository.findByUserIdOrderByReportYearMonthDesc(userId);

        List<String> months = reports.stream()
                .map(MonthlyReport::getReportYearMonth)
                .toList();

        return AvailableMonthsResult.builder()
                .availableMonths(months)
                .build();
    }

    // 통합 조회
    public MonthlyReportUnifiedResult getUnified(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);

        return MonthlyReportUnifiedResult.builder()
                .reportId(report.getReportId())
                .userId(report.getUserId())
                .reportYearMonth(report.getReportYearMonth())
                .analysisJson(report.getAnalysisJson())
                .createdAt(
                        report.getCreatedAt() == null ? null :
                                report.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )
                .build();
    }

    // ✅ 캘린더
    public MonthlyCalendarResult getCalendar(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);

        return MonthlyCalendarResult.builder()
                .reportId(report.getReportId())
                .reportYearMonth(report.getReportYearMonth())
                .calendar(extractNode(report.getAnalysisJson(), "calendar"))
                .build();
    }

    // ✅ 주제
    public MonthlyTopicsResult getTopics(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);

        return MonthlyTopicsResult.builder()
                .reportId(report.getReportId())
                .reportYearMonth(report.getReportYearMonth())
                .topics(extractNode(report.getAnalysisJson(), "topics"))
                .build();
    }

    // ✅ 감정
    public MonthlyEmotionsResult getEmotions(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);

        return MonthlyEmotionsResult.builder()
                .reportId(report.getReportId())
                .reportYearMonth(report.getReportYearMonth())
                .emotions(extractNode(report.getAnalysisJson(), "emotions"))
                .build();
    }

    // 공통 조회
    private MonthlyReport getReport(Long userId, String yearMonth) {
        validateUserId(userId);
        validateYearMonth(yearMonth);

        return monthlyReportRepository.findByUserIdAndReportYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    private void validateYearMonth(String yearMonth) {
        if (yearMonth == null || !YEAR_MONTH_PATTERN.matcher(yearMonth).matches()) {
            throw new CustomException(ErrorCode.REPORT_YEAR_MONTH_INVALID);
        }
    }

    // JSON 안전 추출
    private JsonNode extractNode(JsonNode root, String field) {
        if (root == null || root.isNull()) return null;
        return root.get(field);
    }
}