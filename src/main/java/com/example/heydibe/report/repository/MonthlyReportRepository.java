package com.example.heydibe.report.repository;

import com.example.heydibe.report.domain.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    Optional<MonthlyReport> findByUserIdAndReportYearMonth(Long userId, String reportYearMonth);

    @Query("""
           select mr.reportYearMonth
           from MonthlyReport mr
           where mr.userId = :userId
           order by mr.reportYearMonth asc
           """)
    List<String> findAvailableMonths(Long userId);

    @Query("""
           select max(mr.reportYearMonth)
           from MonthlyReport mr
           where mr.userId = :userId
           """)
    String findDefaultYearMonth(Long userId);
}
