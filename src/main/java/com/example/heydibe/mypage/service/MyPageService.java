package com.example.heydibe.mypage.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.mypage.dto.response.MyPageLikedPostResponse;
import com.example.heydibe.mypage.dto.response.MyPageResponse;
import com.example.heydibe.settings.entity.ReminderSetting;
import com.example.heydibe.settings.repository.ReminderSettingRepository;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ReminderSettingRepository reminderSettingRepository;
    private final JdbcTemplate jdbcTemplate;

    public MyPageResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(null);

        ReminderSetting reminderSetting = reminderSettingRepository.findByUser_Id(userId)
                .orElse(null);

        long likedPostCount = countLikedPosts(userId);
        long sharedPostCount = countSharedPosts(userId);

        return MyPageResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(profile != null ? profile.getProfileImageUrl() : "")
                .likedPostCount(likedPostCount)
                .sharedPostCount(sharedPostCount)
                .alarm(
                        MyPageResponse.Alarm.builder()
                                .enabled(reminderSetting != null && reminderSetting.isEnabled())
                                .build()
                )
                .build();
    }

    public MyPageLikedPostResponse getLikedPosts(Long userId, int page, int size) {
        long totalCount = countLikedPosts(userId);
        int offset = page * size;

        String sql = """
                SELECT
                    u.user_id AS user_id,
                    u.nickname AS nickname,
                    COALESCE(up.profile_image_url, '') AS profile_image_url,
                    p.post_id AS post_id,
                    p.diary_id AS diary_id,
                    p.title AS title,
                    p.content AS preview,
                    p.emotion AS emotion,
                    p.topic AS topic,
                    p.created_at AS created_at
                FROM post_like pl
                JOIN post p ON pl.post_id = p.post_id
                JOIN users u ON p.user_id = u.user_id
                LEFT JOIN user_profile up ON u.user_id = up.user_id
                WHERE pl.user_id = ?
                  AND p.deleted_at IS NULL
                ORDER BY p.created_at DESC, p.post_id DESC
                LIMIT ? OFFSET ?
                """;

        List<MyPageLikedPostResponse.LikedPostItem> posts = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

                    String topic = rs.getString("topic");
                    List<String> topics = splitTopics(topic);

                    return MyPageLikedPostResponse.LikedPostItem.builder()
                            .userId(rs.getLong("user_id"))
                            .nickname(rs.getString("nickname"))
                            .profileImageUrl(rs.getString("profile_image_url"))
                            .postId(rs.getLong("post_id"))
                            .diaryId(rs.getLong("diary_id"))
                            .title(nullToEmpty(rs.getString("title")))
                            .preview(makePreview(nullToEmpty(rs.getString("preview"))))
                            .emotion(nullToEmpty(rs.getString("emotion")))
                            .topics(topics)
                            .createdAt(createdAt)
                            .isLiked(true)
                            .build();
                },
                userId, size, offset
        );

        return MyPageLikedPostResponse.builder()
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .posts(posts)
                .build();
    }

    public Object getMyPosts(Long userId) {
        return List.of();
    }

    private long countLikedPosts(Long userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_like WHERE user_id = ?",
                Long.class,
                userId
        );
        return count != null ? count : 0L;
    }

    private long countSharedPosts(Long userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE user_id = ? AND deleted_at IS NULL",
                Long.class,
                userId
        );
        return count != null ? count : 0L;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String makePreview(String content) {
        if (content.isBlank()) {
            return "";
        }

        int maxLength = 20;
        return content.length() <= maxLength
                ? content
                : content.substring(0, maxLength) + "...";
    }

    private List<String> splitTopics(String topic) {
        if (topic == null || topic.isBlank()) {
            return List.of();
        }

        return List.of(topic.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}