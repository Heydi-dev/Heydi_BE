package com.example.heydibe.mypage.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.mypage.dto.response.MyPageResponse;
import com.example.heydibe.mypage.dto.response.PostSimpleResponse;
import com.example.heydibe.settings.entity.ReminderSetting;
import com.example.heydibe.settings.repository.ReminderSettingRepository;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
                .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
                .likedPostCount(likedPostCount)
                .sharedPostCount(sharedPostCount)
                .alarm(
                        MyPageResponse.Alarm.builder()
                                .enabled(reminderSetting != null && reminderSetting.isEnabled())
                                .build()
                )
                .build();
    }

    public List<PostSimpleResponse> getMyPosts(Long userId) {
        return List.of();
    }

    public List<PostSimpleResponse> getLikedPosts(Long userId) {
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
                "SELECT COUNT(*) FROM post WHERE user_id = ?",
                Long.class,
                userId
        );
        return count != null ? count : 0L;
    }
}