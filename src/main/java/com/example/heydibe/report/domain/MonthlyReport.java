package com.example.heydibe.report.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "monthly_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthly_report_user_month",
                columnNames = {"user_id", "report_year_month"}
        )
)
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // yyyy-MM
    @Column(name = "report_year_month", nullable = false, length = 7)
    private String reportYearMonth;

    // 🔴 DB는 jsonb지만 Java에서는 String으로 유지
    @Column(name = "analysis_json", nullable = false, columnDefinition = "jsonb")
    private String analysisJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MonthlyReport() {}

    public MonthlyReport(Long userId, String reportYearMonth, String analysisJson) {
        this.userId = userId;
        this.reportYearMonth = reportYearMonth;
        this.analysisJson = analysisJson;
        this.createdAt = Instant.now();
    }

    public Long getReportId() {
        return reportId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getReportYearMonth() {
        return reportYearMonth;
    }

    public String getAnalysisJson() {
        return analysisJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
