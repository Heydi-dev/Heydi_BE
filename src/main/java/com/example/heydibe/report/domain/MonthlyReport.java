package com.example.heydibe.report.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "monthly_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_monthly_report_user_month",
                        columnNames = {"user_id", "report_year_month"}
                )
        }
)
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ERD: CHAR(7) "YYYY-MM"
    @Column(name = "report_year_month", nullable = false, length = 7)
    private String reportYearMonth;

    // ERD: JSONB
    @Column(name = "analysis_json", nullable = false, columnDefinition = "jsonb")
    private String analysisJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // getters/setters
    public Long getReportId() { return reportId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReportYearMonth() { return reportYearMonth; }
    public void setReportYearMonth(String reportYearMonth) { this.reportYearMonth = reportYearMonth; }
    public String getAnalysisJson() { return analysisJson; }
    public void setAnalysisJson(String analysisJson) { this.analysisJson = analysisJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
