package com.example.heydibe.infrastructure.s3;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
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

            return buildPublicUrl(objectKey);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public void deleteProfileImage(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return;
        }

        try {
            String objectKey = extractObjectKey(profileImageUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // 삭제 실패는 로그만 남기고 계속 진행
        }
    }

    public String getDefaultProfileImageUrl() {
        return buildPublicUrl(defaultProfileImageKey);
    }

    public String uploadPostImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        validateImageFile(file);

        String objectKey = generatePostImageKey(file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return buildPublicUrl(objectKey);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public String copyPostImageFromUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        String sourceKey = extractObjectKey(sourceUrl);
        String targetKey = generatePostImageKey(sourceKey);

        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(targetKey)
                    .build();
            s3Client.copyObject(copyRequest);

            return buildPublicUrl(targetKey);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public void deletePostImage(String postImageUrl) {
        if (postImageUrl == null || postImageUrl.isBlank()) {
            return;
        }

        try {
            String objectKey = extractObjectKey(postImageUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // 삭제 실패는 로그만 남기고 계속 진행
        }
    }

    public String uploadDiaryImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        validateImageFile(file);

        String objectKey = generateDiaryImageKey(file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return buildPublicUrl(objectKey);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public String copyDiaryImageFromUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        String sourceKey = extractObjectKey(sourceUrl);
        String targetKey = generateDiaryImageKey(sourceKey);

        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(targetKey)
                    .build();
            s3Client.copyObject(copyRequest);

            return buildPublicUrl(targetKey);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public void deleteDiaryImage(String diaryImageUrl) {
        if (diaryImageUrl == null || diaryImageUrl.isBlank()) {
            return;
        }

        try {
            String objectKey = extractObjectKey(diaryImageUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // Ignore delete failures for diary image.
        }
    }

    /**
     * DB/API에 저장된 URL(또는 object key)을 현재 bucket/region 기준 public URL로 변환한다.
     * legacy test-bucket URL도 object key 추출 후 올바른 URL로 재조립한다.
     */
    public String resolvePublicUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return stored;
        }
        return buildPublicUrl(extractObjectKey(stored));
    }

    public String buildPublicUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return objectKey;
        }
        String normalizedKey = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        return getPublicBaseUrl() + "/" + normalizedKey;
    }

    private String getPublicBaseUrl() {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    private String extractObjectKey(String stored) {
        if (stored == null || stored.isBlank()) {
            return stored;
        }

        if (!stored.startsWith("http://") && !stored.startsWith("https://")) {
            return stored.startsWith("/") ? stored.substring(1) : stored;
        }

        try {
            URI uri = URI.create(stored);
            String path = uri.getPath();
            if (path != null && !path.isBlank()) {
                return path.startsWith("/") ? path.substring(1) : path;
            }
        } catch (IllegalArgumentException ignored) {
            // fall through
        }

        return stored;
    }

    private String generateProfileImageKey(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return "profiles/" + uuid + "." + extension;
    }

    private String generatePostImageKey(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return "posts/" + uuid + "." + extension;
    }

    private String generateDiaryImageKey(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return "diary/" + uuid + "." + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
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
