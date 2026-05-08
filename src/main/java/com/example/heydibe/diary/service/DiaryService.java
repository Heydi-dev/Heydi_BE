package com.example.heydibe.diary.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.heydibe.ai.dto.response.TestResponse;
import com.example.heydibe.ai.service.AiService;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.diary.dto.request.DiaryPatchRequest;
import com.example.heydibe.diary.dto.response.DetailedDiaryResponse;
import com.example.heydibe.diary.dto.response.DiariesResponse;
import com.example.heydibe.diary.dto.response.DiaryConversationResponse;
import com.example.heydibe.diary.dto.response.DiaryPatchResponse;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.entity.DiaryAttachment;
import com.example.heydibe.diary.entity.DiaryConversation;
import com.example.heydibe.diary.repository.DiaryAttachmentRepository;
import com.example.heydibe.diary.repository.DiaryConversationRepository;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final AiService aiService;
    private final DiaryRepository diaryRepository;
    private final DiaryAttachmentRepository diaryAttachmentRepository;
    private final DiaryConversationRepository diaryConversationRepository;

    public TestResponse getTest() {
        return aiService.test();
    }

    public DiariesResponse getDiaries(User user, Integer pageNumber, Integer pageSize) {
        DiariesResponse response = new DiariesResponse();
        response.setPage(pageNumber);
        response.setSize(pageSize);

        if (pageNumber < 0 || pageSize <= 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DiariesResponse.DiaryResponse> page = diaryRepository.findAllByUserAndDeletedAtIsNull(user, pageable).map(DiariesResponse.DiaryResponse::from);
        response.setContent(page.getContent());
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return response;
    }

    public DetailedDiaryResponse getDiaryById(Long userId, Long diaryId) {
        DetailedDiaryResponse response = new DetailedDiaryResponse();
        Optional<Diary> temp = diaryRepository.findById(diaryId);
        if (temp.isEmpty() || temp.get().getDeletedAt() != null) {
            throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
        }
        Diary diary = temp.get();

        if (!diary.isPublic() && !diary.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<DiaryAttachment> attachments = diaryAttachmentRepository.findAllByDiaryId(diaryId);

        response.setId(diaryId);
        response.setTitle(diary.getTitle());
        response.setCreatedDate(diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        response.setEmotionCategory(diary.getMainEmotion());
        
        List<String> topics = new ArrayList<>();
        if (diary.getTopic1() != null) {
            topics.add(diary.getTopic1());
        }
        if (diary.getTopic2() != null) {
            topics.add(diary.getTopic2());
        }

        response.setTopic(topics);
        response.setOneLineDiary(diary.getSummaryOneLine());
        response.setContent(diary.getContent());
        response.setConversationSessionId("TODO");
        response.setConversationDurationSec(diary.getConversationDurationSeconds());
        
        List<DetailedDiaryResponse.PhotoResponse> photoResponses = new ArrayList<>();
        for (DiaryAttachment attachment : attachments) {
            photoResponses.add(DetailedDiaryResponse.PhotoResponse.from(attachment));
        }
        response.setPhotos(photoResponses);

        DetailedDiaryResponse.ReportResponse reportResponse = new DetailedDiaryResponse.ReportResponse();
        reportResponse.setIncluded(false); // TODO
        reportResponse.setMonth(diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        response.setReport(reportResponse);

        return response;
    }

    public DiaryPatchResponse patchDiary(Long id, Long diaryId, DiaryPatchRequest request) {
        Optional<Diary> temp = diaryRepository.findById(diaryId);
        if (temp.isEmpty() || temp.get().getDeletedAt() != null) {
            throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
        }
        Diary diary = temp.get();

        if (!diary.getUser().getId().equals(id)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (request.getEmotionCategory() != null) {
            diary.setMainEmotion(request.getEmotionCategory());
        }
        if (request.getTopic() != null) {
            List<String> topics = request.getTopic();
            diary.setTopic1(topics.size() > 0 ? topics.get(0) : null); //TODO: DB diary_tag 반영 필요
            diary.setTopic2(topics.size() > 1 ? topics.get(1) : null);
        }

        if (request.getOneLineDiary() != null) {
            diary.setSummaryOneLine(request.getOneLineDiary());
        }
        if (request.getContent() != null) {
            diary.setContent(request.getContent());
        }

        diary.setUpdatedAt(java.time.LocalDateTime.now());

        diaryRepository.save(diary);

        return new DiaryPatchResponse(diaryId, diary.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
    }

    public DiaryConversationResponse getDiaryConversation(Long userId, Long diaryId) {
        Optional<Diary> temp = diaryRepository.findById(diaryId);
        if (temp.isEmpty() || temp.get().getDeletedAt() != null) {
            throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
        }
        Diary diary = temp.get();

        if (!diary.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<DiaryConversation> conversations = diaryConversationRepository.findAllByDiaryId(diaryId);
        List<DiaryConversationResponse.MessageResponse> messages = conversations.stream()
                .map(DiaryConversationResponse.MessageResponse::from)
                .toList();

        return new DiaryConversationResponse(messages);
    }

    public boolean deleteDiary(Long userId, Long diaryId) {
        Optional<Diary> temp = diaryRepository.findById(diaryId);
        if (temp.isEmpty() || temp.get().getDeletedAt() != null) {
            throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
        }
        Diary diary = temp.get();

        if (!diary.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        diary.setDeletedAt(java.time.LocalDateTime.now());
        diaryRepository.save(diary);

        return true;
    }
}
