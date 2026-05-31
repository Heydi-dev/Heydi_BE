package com.example.heydibe.user.service;

import com.example.heydibe.infrastructure.s3.S3Service;
import com.example.heydibe.user.dto.UserPublicProfile;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPublicProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final S3Service s3Service;

    /**
     * 커뮤니티 등 탈퇴 사용자 콘텐츠가 남아 있을 때 작성자 표시용.
     * 활성 회원이 아니면 닉네임·프로필을 마스킹한다.
     */
    public UserPublicProfile resolve(Long userId) {
        return resolveAll(List.of(userId)).get(userId);
    }

    public Map<Long, UserPublicProfile> resolveAll(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctIds = userIds.stream().distinct().toList();
        Map<Long, User> usersById = userRepository.findAllById(distinctIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, UserProfile> profilesByUserId = userProfileRepository.findByUserIdIn(distinctIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        Map<Long, UserPublicProfile> result = new HashMap<>();
        for (Long userId : distinctIds) {
            User user = usersById.get(userId);
            if (user == null || user.isDeleted()) {
                result.put(userId, UserPublicProfile.withdrawn(userId));
            } else {
                result.put(userId, toActiveProfile(user, profilesByUserId.get(userId)));
            }
        }
        return result;
    }

    private UserPublicProfile toActiveProfile(User user, UserProfile profile) {
        String profileImageUrl = profile != null
                ? s3Service.resolvePublicUrl(profile.getProfileImageUrl())
                : null;
        return new UserPublicProfile(user.getId(), user.getNickname(), profileImageUrl, false);
    }
}
