package top.sshh.qqbot.sdk;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenManagerTest {
    private final AtomicLong clock = new AtomicLong(1_000_000);
    private final HttpClient http = mock(HttpClient.class);
    private final QQBotConfig config = QQBotConfig.builder().appId("app").appSecret("private-secret").build();
    private final TokenManager tokens = new TokenManager(config, http, clock::get);
    private HttpResponse<String> response(String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200); when(response.body()).thenReturn(body); return response;
    }
    @Test void concurrentCallersShareOneTokenRequestAndOneRefresh() throws Exception {
        var first = response("{\"access_token\":\"one\",\"expires_in\":\"7200\"}");
        var second = response("{\"access_token\":\"two\",\"expires_in\":7200}");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(first).thenReturn(second);
        ExecutorService pool = Executors.newFixedThreadPool(12);
        try {
            for (String expected : List.of("one", "two")) {
                List<Future<String>> calls = new ArrayList<>();
                for (int i = 0; i < 32; i++) calls.add(pool.submit(tokens::getToken));
                for (Future<String> call : calls) assertEquals(expected, call.get(5, TimeUnit.SECONDS));
                clock.addAndGet(7_140_000);
            }
            verify(http, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        } finally { pool.shutdownNow(); }
    }
    @Test void stale401CannotInvalidateANewerToken() throws Exception {
        var first = response("{\"access_token\":\"old\",\"expires_in\":100}");
        var second = response("{\"access_token\":\"new\",\"expires_in\":100}");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(first).thenReturn(second);
        assertEquals("old", tokens.getToken()); assertEquals("new", tokens.refreshRejected("old"));
        assertEquals("new", tokens.refreshRejected("old"));
        verify(http, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
    @Test void http200BusinessFailureIsTypedAndCannotLeakCredentials() throws Exception {
        var rejected = response("{\"code\":100016,\"message\":\"private-secret\"}");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(rejected);
        QQApiException failure = assertThrows(QQApiException.class, tokens::getToken);
        assertEquals(100016L, failure.platformCode()); assertFalse(failure.toString().contains("private-secret"));
        assertFalse(config.toString().contains("private-secret"));
    }
    @Test void refreshFailureCanUseUnexpiredCacheButNotAKnownRejectedToken() throws Exception {
        var cached = response("{\"access_token\":\"old\",\"expires_in\":100}");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(cached)
                .thenThrow(new IOException("unavailable"));
        assertEquals("old", tokens.getToken()); clock.addAndGet(90_000); assertEquals("old", tokens.getToken());
        assertThrows(QQApiException.class, () -> tokens.refreshRejected("old"));
    }
    @Test void failureBackoffPreventsConcurrentRefreshStorms() throws Exception {
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException());
        for (int i = 0; i < 20; i++) assertThrows(QQApiException.class, tokens::getToken);
        verify(http, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        clock.addAndGet(1000); assertThrows(QQApiException.class, tokens::getToken);
        verify(http, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
    @Test void expiredDuringNetworkRequestAndInvalidLifetimesAreRejected() throws Exception {
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenAnswer(call -> {
            clock.addAndGet(20_000); return response("{\"access_token\":\"expired\",\"expires_in\":10}");
        });
        assertThrows(QQApiException.class, tokens::getToken);
    }
    @Test void interruptionRestoresTheThreadFlag() throws Exception {
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new InterruptedException());
        try { assertThrows(QQApiException.class, tokens::getToken); assertTrue(Thread.currentThread().isInterrupted()); }
        finally { Thread.interrupted(); }
    }
    @Test void missingCredentialsFailBeforeAnyNetworkRequest() {
        TokenManager missing = new TokenManager(QQBotConfig.builder().build(), http);
        assertThrows(QQApiException.class, missing::getToken); verifyNoInteractions(http);
    }
}
