package com.example.heydibe.diary.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.entity.DiaryAttachment;
import com.example.heydibe.diary.dto.response.DiaryPhotoListResponse;
import com.example.heydibe.diary.dto.response.DiaryPhotoUploadResponse;
import com.example.heydibe.diary.repository.DiaryAttachmentRepository;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.infrastructure.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryPhotoService {

    private static final int MAX_PHOTOS = 4;

    private final DiaryRepository diaryRepository;
    private final DiaryAttachmentRepository diaryAttachmentRepository;
    private final S3Service s3Service;

    @Transactional
    public DiaryPhotoUploadResponse addPhoto(Long userId, Long diaryId, MultipartFile photo) {
        Diary diary = getOwnedDiary(userId, diaryId);

        long count = diaryAttachmentRepository.countByDiaryId(diary.getId()); // 현재 다이어리에 등록된 사진 개수 조회
        if (count >= MAX_PHOTOS) { // 이미 최대 개수에 도달함
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        String fileUrl = s3Service.uploadDiaryImage(photo);
        DiaryAttachment attachment = DiaryAttachment.builder()
                .diary(diary)
                .fileUrl(fileUrl)
                .fileType(photo.getContentType())
                .build();
        DiaryAttachment saved = diaryAttachmentRepository.save(attachment);

        return new DiaryPhotoUploadResponse(saved.getId(), saved.getFileUrl());
    }

    @Transactional
    public void deletePhoto(Long userId, Long diaryId, Long fileId) {
        Diary diary = getOwnedDiary(userId, diaryId);

        DiaryAttachment attachment = diaryAttachmentRepository.findByIdAndDiaryId(fileId, diary.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        s3Service.deleteDiaryImage(attachment.getFileUrl());
        diaryAttachmentRepository.delete(attachment);
    }

    @Transactional
    public DiaryPhotoListResponse getPhotos(Long userId, Long diaryId) {
        Diary diary = getOwnedDiary(userId, diaryId);

        List<DiaryAttachment> attachments = diaryAttachmentRepository.findByDiaryIdOrderByIdAsc(diary.getId());
        List<DiaryPhotoListResponse.Photo> photos = new ArrayList<>();
        for (int i = 0; i < attachments.size(); i++) {
            DiaryAttachment attachment = attachments.get(i);
            photos.add(new DiaryPhotoListResponse.Photo(
                    attachment.getId(),
                    attachment.getFileUrl(),
                    i + 1
            ));
        }

        return new DiaryPhotoListResponse(photos);
    }

    private Diary getOwnedDiary(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        if (diary.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
        }

        if (!diary.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return diary;
    }
}
