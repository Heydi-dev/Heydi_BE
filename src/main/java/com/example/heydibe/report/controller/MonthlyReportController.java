package com.example.heydibe.report.controller;

import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.service.MonthlyReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.heydibe.report.controller.MonthlyReportDto.*;

@RestController
@RequestMapping("/api/reports")
public class MonthlyReportController {

    private final MonthlyReportService service;

    public MonthlyReportController(MonthlyReportService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @GetMapping("/{userId}/{yearMonth}")
    public ResponseEntity<Response> get(@PathVariable Long userId, @PathVariable String yearMonth) {
        try {
            MonthlyReport r = service.get(userId, yearMonth);
            return ResponseEntity.ok(toResponse(r));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{userId}/{yearMonth}")
    public ResponseEntity<Response> upsert(
            @PathVariable Long userId,
            @PathVariable String yearMonth,
            @RequestBody UpsertRequest req
    ) {
        MonthlyReport r = service.upsert(userId, yearMonth, req.analysisJson());
        return ResponseEntity.ok(toResponse(r));
    }

    private Response toResponse(MonthlyReport r) {
        return new Response(
                r.getReportId(),
                r.getUserId(),
                r.getReportYearMonth(),
                r.getAnalysisJson(),
                r.getCreatedAt()
        );
    }
}
