package com.example.heydibe.community.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "post_attachment",
        indexes = @Index(name = "idx_post_attachment_post_id", columnList = "post_id")
)
public class PostAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
}
