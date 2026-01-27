package com.example.heydibe.infrastructure.s3;

import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.presigned.expire-seconds:300}")
    private long expireSeconds;

    @Value("${app.profile-image-base-url:https://test-bucket.s3.ap-northeast-2.amazonaws.com}")
    private String profileImageBaseUrl;

    @Value("${app.default-profile-image-key:profiles/default.png}")
    private String defaultProfileImageKey;

    public String generatePresignedUrl(String objectKey, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expireSeconds))
                    .putObjectRequest(putObjectRequest)
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public String uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            // 파일이 없으면 null 반환 (프론트엔드에서 기본 이미지 처리)
            return null;
        }

        validateImageFile(file);

        String objectKey = generateProfileImageKey(file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String baseUrl = profileImageBaseUrl.endsWith("/") 
                    ? profileImageBaseUrl.substring(0, profileImageBaseUrl.length() - 1)
                    : profileImageBaseUrl;
            return baseUrl + "/" + objectKey;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public void deleteProfileImage(String profileImageUrl) {
        // null이거나 빈 문자열이면 삭제할 이미지가 없음
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return;
        }

        try {
            String objectKey = extractObjectKeyFromUrl(profileImageUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // 삭제 실패는 로그만 남기고 계속 진행
        }
    }

    /**
     * 기본 프로필 이미지 URL 반환
     * S3에 업로드된 기본 이미지 URL을 반환
     */
    public String getDefaultProfileImageUrl() {
        String baseUrl = profileImageBaseUrl.endsWith("/") 
                ? profileImageBaseUrl.substring(0, profileImageBaseUrl.length() - 1)
                : profileImageBaseUrl;
        return baseUrl + "/" + defaultProfileImageKey;
    }

    private String generateProfileImageKey(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return "profiles/" + uuid + "." + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String extractObjectKeyFromUrl(String url) {
        if (url.contains(profileImageBaseUrl + "/")) {
            return url.substring(url.indexOf(profileImageBaseUrl + "/") + profileImageBaseUrl.length() + 1);
        }
        return url;
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }
}
