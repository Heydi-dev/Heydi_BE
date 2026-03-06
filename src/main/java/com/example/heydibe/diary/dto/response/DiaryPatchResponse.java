package com.example.heydibe.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryPatchResponse {
    private Long id;
    private String updatedDate; // "2025-11-21T21:23:00"
}
