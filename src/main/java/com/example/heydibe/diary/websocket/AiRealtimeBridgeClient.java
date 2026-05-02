package com.example.heydibe.diary.websocket;

public interface AiRealtimeBridgeClient {

    AiBridgeSession connect(Long userId, AiBridgeListener listener);

    interface AiBridgeSession {
        void sendBinary(byte[] payload);

        void close();
    }

    interface AiBridgeListener {
        void onText(String payload);

        void onBinary(byte[] payload);

        void onClosed(int statusCode, String reason);

        void onError(Throwable throwable);
    }
}

