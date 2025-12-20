package com.example.heydibe.file.controller;

import com.example.heydibe.common.ApiResponse;
import com.example.heydibe.file.request.PresignedUrlRequest;
import com.example.heydibe.file.response.PresignedUrlResponse;
import com.example.heydibe.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponse> generatePresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        return ApiResponse.success(fileService.generatePresignedUrl(request));
    }
}

