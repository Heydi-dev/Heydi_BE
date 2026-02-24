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
    private Long diary_id;

    @NotBlank
    private String post_title;

    @NotNull
    private LocalDate diary_date;

    private Integer conversation_duration;

    @NotBlank
    private String post_emotion;

    @NotBlank
    private String post_content;

    @NotNull
    private List<String> post_topics;

    private List<ExistingPhoto> existing_photos;

    @Getter
    @Setter
    public static class ExistingPhoto {
        @NotBlank
        private String image_url;
    }
}
