package com.example.heydibe.community.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PostCreateRequest {

    @NotNull
    private Long diaryId;

    @NotBlank
    private String postTitle;

    @NotNull
    private LocalDate diaryDate;

    private Integer conversationDuration;

    @NotBlank
    private String postEmotion;

    @NotBlank
    private String postContent;

    @NotNull
    private List<String> postTopics;

    private List<ExistingPhoto> existingPhotos;

    @Getter
    @Setter
    public static class ExistingPhoto {
        @NotBlank
        private String imageUrl;
    }
}
