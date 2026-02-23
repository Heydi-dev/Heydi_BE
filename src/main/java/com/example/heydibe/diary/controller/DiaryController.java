package com.example.heydibe.diary.controller;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/diaries")
public class DiaryController {
    @GetMapping
    public String getDiaries() {
        return "get diaries - TODO";
    }

    @GetMapping("/{diaryId}")
    public String getDiaryById(@PathVariable Long diaryId) {
        return "get diary by id: " + diaryId + " - TODO";
    }

    @PutMapping("/{diaryId}")
    public String putDiary(@PathVariable Long diaryId, @RequestBody String entity) {
        return "put diary by id: " + diaryId + " with entity: " + entity + " - TODO";
    }

    @DeleteMapping("/{diaryId}")
    public String deleteDiary(@PathVariable Long diaryId) {
        return "delete diary by id: " + diaryId + " - TODO";
    }

    @GetMapping("/{diaryId}/conversation")
    public String getConversation(@PathVariable Long diaryId, @RequestParam String param) {
        return "get conversation for diary id: " + diaryId + " with param: " + param + " - TODO";
    }

    @GetMapping("/{diaryId}/photos")
    public String getPhotos(@PathVariable Long diaryId) {
        return "get photos for diary id: " + diaryId + " - TODO";
    }

    @PostMapping("/{diaryId}/photos")
    public String postPhotos(@PathVariable Long diaryId, @RequestBody String entity) {

        return "posted photos for diary id: " + diaryId + " with entity: " + entity + " - TODO";
    }

    @DeleteMapping("/{diaryId}/photos/{photoId}")
    public String deletePhotos(@PathVariable Long diaryId, @PathVariable Long photoId) {
        return "deleted photo id: " + photoId + " for diary id: " + diaryId + " - TODO";
    }

    @PostMapping("/{diaryId}/export/pdf")
    public String exportPdf(@PathVariable Long diaryId) {
        return "exported pdf for diary id: " + diaryId + " - TODO";
    }
}
