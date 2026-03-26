package com.example.heydibe.community.post.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostPhotoUploadRequest {

    @NotNull
    private MultipartFile photo;
}
