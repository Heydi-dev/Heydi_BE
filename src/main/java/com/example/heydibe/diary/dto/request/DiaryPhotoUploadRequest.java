package com.example.heydibe.diary.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DiaryPhotoUploadRequest {

    @NotNull
    private MultipartFile photo;
}
