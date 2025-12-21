package com.example.heydibe.report.service;

import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyReportService {

    private final MonthlyReportRepository repo;

    public MonthlyReportService(MonthlyReportRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public MonthlyReport get(Long userId, String yearMonth) {
        return repo.findByUserIdAndReportYearMonth(userId, yearMonth)
                .orElseThrow(() -> new IllegalArgumentException("REPORT_NOT_FOUND"));
    }

    @Transactional
    public MonthlyReport upsert(Long userId, String yearMonth, String analysisJson) {
        MonthlyReport r = repo.findByUserIdAndReportYearMonth(userId, yearMonth)
                .orElseGet(MonthlyReport::new);

        r.setUserId(userId);
        r.setReportYearMonth(yearMonth);
        r.setAnalysisJson(analysisJson);

        return repo.save(r);
    }
}
