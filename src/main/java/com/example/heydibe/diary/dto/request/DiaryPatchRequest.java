package com.example.heydibe.diary.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryPatchRequest {
    private String emotionCategory;
    private List<String> topic;
    private String oneLineDiary;
    private String content;
}
