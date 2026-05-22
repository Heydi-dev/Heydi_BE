package com.example.heydibe.notification;

import com.example.heydibe.notification.service.FcmService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmTestRunner implements CommandLineRunner {

    private final FcmService fcmService;

    @Override
    public void run(String... args) {
        // 테스트 끝나면 false로 바꾸면 서버 실행 시 자동 전송 안 함
        boolean enabled = true;

        if (!enabled) {
            return;
        }

        try {
            // 여기에 실제 FCM 토큰 넣기
            String token = "cJRz2hXOJlS2-PPXvZXNuY:APA91bE_0ijxb_pCNVR62ml784DzcSYXfLBh-dtrp32D619e9c2mwRYfiPgKodYvoZnEThYp_o2Sl07ISADFxoKrlONYs00bbcFBlAw8FUBNI_w5NnUHpSE";

            String response = fcmService.sendMessage(
                    token,
                    "테스트 알림",
                    "FCM 전송 테스트입니다."
            );

            log.info("FCM test completed: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM test failed", e);
        }
    }
}