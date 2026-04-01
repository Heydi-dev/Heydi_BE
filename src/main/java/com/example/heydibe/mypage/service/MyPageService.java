package com.example.heydibe.mypage.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.mypage.dto.response.MyPageResponse;
import com.example.heydibe.mypage.dto.response.PostSimpleResponse;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // ✅ 마이페이지 메인
    public MyPageResponse getMyPage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(null);

        return MyPageResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
                .build();
    }

    // ✅ 내가 쓴 글
    public List<PostSimpleResponse> getMyPosts(Long userId) {
        // TODO: 실제 PostRepository 연결
        return List.of(); // 임시
    }

    // ✅ 좋아요 한 글
    public List<PostSimpleResponse> getLikedPosts(Long userId) {
        // TODO: 실제 LikeRepository 연결
        return List.of(); // 임시
    }
}