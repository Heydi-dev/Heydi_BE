package com.example.heydibe.report.repository;

import com.example.heydibe.report.domain.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    Optional<MonthlyReport> findByUserIdAndReportYearMonth(Long userId, String reportYearMonth);

    @Query("""
        select distinct m.reportYearMonth
        from MonthlyReport m
        where m.userId = :userId
        order by m.reportYearMonth desc
    """)
    List<String> findAvailableMonths(Long userId);

    @Query("""
        select max(m.reportYearMonth)
        from MonthlyReport m
        where m.userId = :userId
    """)
    String findDefaultYearMonth(Long userId);

    // 🔥 jsonb 캐스팅 UPDATE (이게 없어서 터졌던 거)
    @Modifying
    @Query(
            value = """
            UPDATE monthly_report
            SET analysis_json = CAST(:analysisJson AS jsonb)
            WHERE report_id = :reportId
        """,
            nativeQuery = true
    )
    void updateAnalysisJson(Long reportId, String analysisJson);
}
