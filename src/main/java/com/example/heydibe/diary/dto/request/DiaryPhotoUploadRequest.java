package com.example.heydibe.diary.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class DiaryPhotoUploadRequest {

    @NotEmpty
    private List<MultipartFile> photos;
}
