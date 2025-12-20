package com.example.heydibe.file.service;

import com.example.heydibe.file.request.PresignedUrlRequest;
import com.example.heydibe.file.response.PresignedUrlResponse;
import com.example.heydibe.file.s3.S3Client;
import com.example.heydibe.file.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Client s3Client;

    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        String objectKey = generateObjectKey(request.getFolder(), request.getExtension());
        String url = createPresignedUrl(objectKey, request.getContentType());
        return PresignedUrlResponse.from(url, objectKey);
    }

    public String generateObjectKey(String folder, String extension) {
        String uuid = UUIDUtil.generate();
        String safeFolder = folder.trim();
        String safeExt = extension.trim().toLowerCase();
        return safeFolder + "/" + uuid + "." + safeExt;
    }

    public String createPresignedUrl(String objectKey, String contentType) {
        return s3Client.generatePresignedUrl(objectKey, contentType);
    }
}
