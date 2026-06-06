package com.example.heydibe.diary.service;

import com.example.heydibe.ai.service.AiService;
import com.example.heydibe.diary.dto.request.DiaryPatchRequest;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.repository.DiaryAttachmentRepository;
import com.example.heydibe.diary.repository.DiaryConversationRepository;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.report.service.MonthlyReportUpdateService;
import com.example.heydibe.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private DiaryAttachmentRepository diaryAttachmentRepository;

    @Mock
    private DiaryConversationRepository diaryConversationRepository;

    @Mock
    private MonthlyReportUpdateService monthlyReportUpdateService;

    @InjectMocks
    private DiaryService diaryService;

    @Test
    void patchDiary_updatesMonthlyReportAfterSavingDiary() {
        Long userId = 1L;
        Long diaryId = 10L;
        Diary diary = createDiary(diaryId, userId);
        DiaryPatchRequest request = new DiaryPatchRequest();
        request.setEmotionCategory("기쁨");

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        diaryService.patchDiary(userId, diaryId, request);

        verify(diaryRepository).save(diary);
        verify(monthlyReportUpdateService).updateReportForDiary(diary);
    }

    private Diary createDiary(Long diaryId, Long userId) {
        User user = User.builder()
                .id(userId)
                .username("tester")
                .nickname("tester")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Diary diary = new Diary();
        diary.setId(diaryId);
        diary.setUser(user);
        diary.setContent("old content");
        diary.setMainEmotion("무난함");
        diary.setCreatedAt(LocalDateTime.now().minusDays(1));
        diary.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return diary;
    }
}
