package com.example.heydibe.report.service;

import com.example.heydibe.common.api.ApiException;
import com.example.heydibe.diary.domain.Diary;
import com.example.heydibe.diary.domain.DiaryTag;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.diary.repository.DiaryTagRepository;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class MonthlyReportQueryService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MonthlyReportRepository monthlyReportRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final ObjectMapper objectMapper;

    public MonthlyReportQueryService(
            MonthlyReportRepository monthlyReportRepository,
            DiaryRepository diaryRepository,
            DiaryTagRepository diaryTagRepository,
            ObjectMapper objectMapper
    ) {
        this.monthlyReportRepository = monthlyReportRepository;
        this.diaryRepository = diaryRepository;
        this.diaryTagRepository = diaryTagRepository;
        this.objectMapper = objectMapper;
    }

    // ========================
    // 🔥 C 단계: 월간 리포트 저장
    // ========================
    @Transactional
    public MonthlyReportUpsertResult upsertMonthlyReport(Long userId, String yearMonth, Object analysis) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode node = objectMapper.valueToTree(analysis);
            if (!node.isObject()) {
                throw new ApiException(6801, "analysis_json 형식이 올바르지 않습니다.");
            }

            String json = objectMapper.writeValueAsString(node);

            MonthlyReport report = monthlyReportRepository
                    .findByUserIdAndReportYearMonth(userId, ym.toString())
                    .orElseGet(() ->
                            monthlyReportRepository.save(
                                    new MonthlyReport(userId, ym.toString(), json)
                            )
                    );

            // 🔥 jsonb 캐스팅 UPDATE
            monthlyReportRepository.updateAnalysisJson(report.getReportId(), json);

            return new MonthlyReportUpsertResult(ym.toString());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6801, "월간 리포트를 저장하지 못했습니다.");
        }
    }

    // ========================
    // A) /reports/monthly
    // ========================
    public AvailableMonthsResult getAvailableMonths(Long userId) {
        List<String> months = monthlyReportRepository.findAvailableMonths(userId);
        String defaultYm = monthlyReportRepository.findDefaultYearMonth(userId);
        return new AvailableMonthsResult(months, defaultYm);
    }

    // ========================
    // B) calendar
    // ========================
    public CalendarResult getCalendar(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        Instant start = startOfMonth(ym);
        Instant end = startOfMonth(ym.plusMonths(1));

        List<Diary> diaries = diaryRepository.findDiariesInRange(userId, start, end);

        List<CalendarEntry> entries = diaries.stream()
                .map(d -> new CalendarEntry(
                        DATE_FMT.format(d.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                        d.getDiaryId()
                ))
                .toList();

        return new CalendarResult(yearMonth, entries);
    }

    // ========================
    // 내부 헬퍼
    // ========================
    private YearMonth parseYearMonthOrThrow(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new ApiException(4000, "yearMonth 형식이 올바르지 않습니다. (예: 2025-12)");
        }
    }

    private Instant startOfMonth(YearMonth ym) {
        return ym.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Stream<JsonNode> streamArray(JsonNode arr) {
        Iterator<JsonNode> it = arr.elements();
        Spliterator<JsonNode> sp = Spliterators.spliteratorUnknownSize(it, 0);
        return StreamSupport.stream(sp, false);
    }
}
