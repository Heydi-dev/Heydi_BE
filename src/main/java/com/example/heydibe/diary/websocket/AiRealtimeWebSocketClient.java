package com.example.heydibe.diary.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Slf4j
@Component
public class AiRealtimeWebSocketClient implements AiRealtimeBridgeClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.ai-server.base-url}")
    private String aiBaseUrl;

    @Value("${app.ai-server.ws-path-template:/api/v1/model/ws/conversations?user_id=%d}")
    private String wsPathTemplate;

    @Override
    public AiBridgeSession connect(Long userId, AiBridgeListener listener) {
        log.info("Connecting AI realtime websocket for userId={}", userId);
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(listener, "listener");

        URI uri = buildWsUri(userId);
        log.info("Connecting AI realtime websocket: {}", uri);

        ForwardingListener forwardingListener = new ForwardingListener(listener);
        WebSocket socket;
        try {
            socket = httpClient.newWebSocketBuilder()
                    .buildAsync(uri, forwardingListener)
                    .join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("Failed to connect AI websocket", cause);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to connect AI websocket", e);
        }

        return new AiBridgeSession() {
            @Override
            public void sendBinary(byte[] payload) {
                socket.sendBinary(ByteBuffer.wrap(payload), true).join();
            }

            @Override
            public void close() {
                try {
                    socket.sendClose(WebSocket.NORMAL_CLOSURE, "closed").join();
                } catch (Exception e) {
                    log.debug("AI websocket close failed: {}", e.getMessage());
                }
            }
        };
    }

    private URI buildWsUri(Long userId) {
        String resolvedPath = String.format(wsPathTemplate, userId);
        if (resolvedPath.startsWith("ws://") || resolvedPath.startsWith("wss://")) {
            return URI.create(resolvedPath);
        }

        URI baseUri = URI.create(aiBaseUrl);
        String scheme = "https".equalsIgnoreCase(baseUri.getScheme()) ? "wss" : "ws";

        String host = baseUri.getHost();
        int port = baseUri.getPort();

        String path = resolvedPath.startsWith("/") ? resolvedPath : "/" + resolvedPath;
        String finalUri = scheme + "://" + host + (port > 0 ? ":" + port : "") + path;
        return URI.create(finalUri);
    }

    private static final class ForwardingListener implements WebSocket.Listener {
        private final AiBridgeListener delegate;
        private final StringBuilder textBuffer = new StringBuilder();
        private final ByteArrayOutputStream binaryBuffer = new ByteArrayOutputStream();

        private ForwardingListener(AiBridgeListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("AI websocket opened: {}", webSocket);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                delegate.onText(textBuffer.toString());
                textBuffer.setLength(0);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            binaryBuffer.write(bytes, 0, bytes.length);
            if (last) {
                delegate.onBinary(binaryBuffer.toByteArray());
                binaryBuffer.reset();
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            delegate.onClosed(statusCode, reason);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            delegate.onError(error);
        }
    }
}
