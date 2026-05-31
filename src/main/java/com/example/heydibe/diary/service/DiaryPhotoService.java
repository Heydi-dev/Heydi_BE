package com.example.heydibe.diary.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.diary.dto.response.DiaryPhotoListResponse;
import com.example.heydibe.diary.dto.response.DiaryPhotoUploadResponse;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.entity.DiaryAttachment;
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
    public DiaryPhotoUploadResponse createPhotos(Long userId, Long diaryId, List<MultipartFile> photos) {
        Diary diary = getOwnedDiary(userId, diaryId);

        validatePhotosInput(photos);

        long count = diaryAttachmentRepository.countByDiaryId(diary.getId());
        if (count + photos.size() > MAX_PHOTOS) {
            throw new CustomException(ErrorCode.DIARY_PHOTO_LIMIT_EXCEEDED);
        }

        return savePhotos(diary, photos);
    }

    @Transactional
    public DiaryPhotoUploadResponse replacePhotos(Long userId, Long diaryId, List<MultipartFile> photos) {
        Diary diary = getOwnedDiary(userId, diaryId);

        validatePhotosInput(photos);

        List<DiaryAttachment> existingAttachments = diaryAttachmentRepository.findByDiaryIdOrderByIdAsc(diary.getId());
        for (DiaryAttachment attachment : existingAttachments) {
            s3Service.deleteDiaryImage(attachment.getFileUrl());
        }
        if (!existingAttachments.isEmpty()) {
            diaryAttachmentRepository.deleteAll(existingAttachments);
        }

        return savePhotos(diary, photos);
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
        for (DiaryAttachment attachment : attachments) {
            photos.add(new DiaryPhotoListResponse.Photo(
                    attachment.getId(),
                    s3Service.resolvePublicUrl(attachment.getFileUrl())
            ));
        }

        return new DiaryPhotoListResponse(photos);
    }

    private DiaryPhotoUploadResponse savePhotos(Diary diary, List<MultipartFile> photos) {
        List<DiaryPhotoUploadResponse.Photo> uploadedPhotos = new ArrayList<>();

        for (MultipartFile photo : photos) {
            String fileUrl = s3Service.uploadDiaryImage(photo);
            DiaryAttachment attachment = DiaryAttachment.builder()
                    .diary(diary)
                    .fileUrl(fileUrl)
                    .fileType(photo.getContentType())
                    .build();
            DiaryAttachment saved = diaryAttachmentRepository.save(attachment);

            uploadedPhotos.add(new DiaryPhotoUploadResponse.Photo(
                    saved.getId(),
                    s3Service.resolvePublicUrl(saved.getFileUrl())
            ));
        }

        return new DiaryPhotoUploadResponse(uploadedPhotos);
    }

    private void validatePhotosInput(List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        if (photos.size() > MAX_PHOTOS) {
            throw new CustomException(ErrorCode.DIARY_PHOTO_LIMIT_EXCEEDED);
        }
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
