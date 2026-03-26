package com.example.heydibe.diary.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class DiaryPhotoUploadRequest {

    @NotEmpty
    @Size(max = 4)
    private List<MultipartFile> photos;
}
