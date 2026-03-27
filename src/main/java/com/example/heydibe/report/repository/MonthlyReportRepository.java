package com.example.heydibe.report.repository;

import com.example.heydibe.report.domain.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    List<MonthlyReport> findByUserIdOrderByReportYearMonthDesc(Long userId);

    Optional<MonthlyReport> findByUserIdAndReportYearMonth(Long userId, String reportYearMonth);
}