package com.example.heydibe.report.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.dto.MonthlyReportApiDto.AvailableMonthsResult;
import com.example.heydibe.report.dto.MonthlyReportApiDto.MonthlyReportUnifiedResult;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyReportQueryService {

    private final MonthlyReportRepository monthlyReportRepository;

    public AvailableMonthsResult getAvailableMonths(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        try {
            List<MonthlyReport> reports =
                    monthlyReportRepository.findByUserIdOrderByReportYearMonthDesc(userId);

            List<String> availableMonths = reports.stream()
                    .map(MonthlyReport::getReportYearMonth)
                    .toList();

            return AvailableMonthsResult.builder()
                    .availableMonths(availableMonths)
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("getAvailableMonths 실패 - userId={}", userId, e);
            throw new CustomException(ErrorCode.MONTHLY_REPORT_LIST_FETCH_FAILED);
        }
    }

    public MonthlyReportUnifiedResult getUnified(Long userId, String yearMonth) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        if (yearMonth == null || yearMonth.isBlank()) {
            throw new CustomException(ErrorCode.REPORT_YEAR_MONTH_INVALID);
        }

        try {
            MonthlyReport report = monthlyReportRepository
                    .findByUserIdAndReportYearMonth(userId, yearMonth)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

            log.info("월간 리포트 조회 성공 - reportId={}, userId={}, yearMonth={}, analysisJson={}",
                    report.getReportId(),
                    report.getUserId(),
                    report.getReportYearMonth(),
                    report.getAnalysisJson());

            return MonthlyReportUnifiedResult.builder()
                    .reportId(report.getReportId())
                    .userId(report.getUserId())
                    .reportYearMonth(report.getReportYearMonth())
                    .analysisJson(report.getAnalysisJson())
                    .createdAt(
                            report.getCreatedAt() == null
                                    ? null
                                    : report.getCreatedAt()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                    )
                    .build();

        } catch (CustomException e) {
            log.warn("getUnified CustomException - userId={}, yearMonth={}, message={}",
                    userId, yearMonth, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("getUnified 실패 - userId={}, yearMonth={}", userId, yearMonth, e);
            throw new CustomException(ErrorCode.MONTHLY_REPORT_FETCH_FAILED);
        }
    }
}