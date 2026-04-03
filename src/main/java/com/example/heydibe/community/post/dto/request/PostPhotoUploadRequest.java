package com.example.heydibe.community.post.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostPhotoUploadRequest {

    @NotEmpty
    private List<MultipartFile> photos;
}
