package com.example.heydibe.diary.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.heydibe.ai.dto.response.TestResponse;
import com.example.heydibe.ai.service.AiService;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.diary.dto.response.DiariesResponse;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final AiService aiService;
    private final DiaryRepository diaryRepository;

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
}
