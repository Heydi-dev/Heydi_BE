package com.example.heydibe.community.post.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostSelectDiaryRequest {

    @NotNull
    private Long diary_id;
}
