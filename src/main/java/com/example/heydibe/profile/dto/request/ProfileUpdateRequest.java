package com.example.heydibe.profile.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileUpdateRequest {
    private MultipartFile profileImage;
    private String currentPassword;
    private String newPassword;
    private String nickname;
    private String deleteProfileImage;
}
