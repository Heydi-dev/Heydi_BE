package com.example.heydibe.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "SignUpRequest")
public class SignUpSwaggerRequest {

    @Schema(description = "로그인에 사용될 유저네임", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "로그인 비밀번호", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "사용자 닉네임", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @Schema(description = "프로필 이미지 파일", type = "string", format = "binary", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String profileImage;
}
