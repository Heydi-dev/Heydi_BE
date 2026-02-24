package com.example.heydibe.community.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCommentUpdateRequest {

    @NotBlank
    private String content;
}
