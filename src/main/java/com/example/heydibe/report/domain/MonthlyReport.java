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

    // 'YYYY-MM'
    @Column(name = "report_year_month", nullable = false, length = 7)
    private String reportYearMonth;

    @Column(name = "analysis_json", nullable = false, columnDefinition = "jsonb")
    private String analysisJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MonthlyReport() {}

    public Long getReportId() { return reportId; }
    public Long getUserId() { return userId; }
    public String getReportYearMonth() { return reportYearMonth; }
    public String getAnalysisJson() { return analysisJson; }
    public Instant getCreatedAt() { return createdAt; }
}
