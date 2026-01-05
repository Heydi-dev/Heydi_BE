package com.example.heydibe.diary.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "diary_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_diary_tag_unique",
                columnNames = {"diary_id", "tag_name", "source_type"}
        )
)
public class DiaryTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @Column(name = "tag_category", nullable = false, length = 20)
    private String tagCategory; // 'ACTIVITY','PERSON','PLACE','TOPIC'

    @Column(name = "source_type", nullable = false, length = 10)
    private String sourceType; // 'USER','AI'

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DiaryTag() {}

    public Long getTagId() { return tagId; }
    public Long getDiaryId() { return diaryId; }
    public String getTagName() { return tagName; }
    public String getTagCategory() { return tagCategory; }
    public String getSourceType() { return sourceType; }
    public Instant getCreatedAt() { return createdAt; }
}
