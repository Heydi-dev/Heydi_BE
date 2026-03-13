package com.example.heydibe.report.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.report.dto.response.MonthlyReportEntryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonthlyReportEntryService {
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final DiaryRepository diaryRepository;

    public MonthlyReportEntryResponse includeDiaryInMonthlyReport(Long userId, String yearMonth, Long diaryId) {
        if (diaryId == null) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        YearMonth requestedMonth = parseYearMonth(yearMonth);

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        if (diary.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
        }

        if (!diary.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        YearMonth diaryMonth = YearMonth.from(diary.getCreatedAt());
        if (!diaryMonth.equals(requestedMonth)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if (Boolean.TRUE.equals(diary.getIncludedInMonthlyReport())) {
            throw new CustomException(ErrorCode.DIARY_ALREADY_INCLUDED_IN_REPORT);
        }

        diary.setIncludedInMonthlyReport(true);
        diary.setUpdatedAt(LocalDateTime.now());
        diaryRepository.save(diary);

        return new MonthlyReportEntryResponse(diary.getId(), requestedMonth.format(YEAR_MONTH_FORMATTER));
    }

    private YearMonth parseYearMonth(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_YEAR_MONTH_FORMAT);
        }
    }
}
