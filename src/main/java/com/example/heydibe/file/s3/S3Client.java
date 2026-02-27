package com.example.heydibe.file.s3;

import com.example.heydibe.common.exception.BusinessException;
import com.example.heydibe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3Client {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presigned.expire-seconds:300}")
    private long expireSeconds;

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
            throw new BusinessException(ErrorCode.S3_PRESIGNED_FAILED);
        }
    }
}

