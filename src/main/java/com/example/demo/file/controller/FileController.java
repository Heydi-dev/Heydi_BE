package com.example.demo.file.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.file.request.PresignedUrlRequest;
import com.example.demo.file.response.PresignedUrlResponse;
import com.example.demo.file.service.FileService;
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
