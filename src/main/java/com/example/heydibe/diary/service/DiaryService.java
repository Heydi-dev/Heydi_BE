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
import com.example.heydibe.diary.dto.response.DetailedDiaryResponse;
import com.example.heydibe.diary.dto.response.DiariesResponse;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.entity.DiaryAttachment;
import com.example.heydibe.diary.repository.DiaryAttachmentRepository;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final AiService aiService;
    private final DiaryRepository diaryRepository;
    private final DiaryAttachmentRepository diaryAttachmentRepository;

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
        Page<DiariesResponse.DiaryResponse> page = diaryRepository.findAllByUser(user, pageable).map(DiariesResponse.DiaryResponse::from);
        response.setContent(page.getContent());
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return response;
    }

    public DetailedDiaryResponse getDiaryById(Long userId, Long diaryId) {
        DetailedDiaryResponse response = new DetailedDiaryResponse();
        Optional<Diary> temp = diaryRepository.findById(diaryId);
        if (temp.isEmpty()){
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
        response.setConversationDurationSec(1); // TODO
        
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
}
