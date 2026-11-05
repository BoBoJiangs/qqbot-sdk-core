package top.sshh.qqbot.sdk;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 驱动真实监听器和重连任务，无真实网络、线程或等待。 */
class GatewayClientTest {
    private final AtomicLong now = new AtomicLong(1_000_000);
    private final List<Connection> connections = new ArrayList<>();
    private final List<Task> delayed = new ArrayList<>();
    private final List<Task> heartbeats = new ArrayList<>();
    private GatewayClient client;
    private ApiClient api;
    private AccessTokenProvider tokens;
    private BotEventHandler dispatcher;
    private ScheduledExecutorService scheduler;
    private HttpClient http;
    private boolean helloBeforeConnectCompletes;

    @BeforeEach
    void setUp() {
        api = mock(ApiClient.class);
        tokens = mock(AccessTokenProvider.class);
        dispatcher = mock(BotEventHandler.class);
        scheduler = mock(ScheduledExecutorService.class);
        http = mock(HttpClient.class);
        WebSocket.Builder builder = mock(WebSocket.Builder.class);
        when(api.gatewayUrl()).thenReturn("wss://gateway.example.test/websocket");
        when(tokens.getToken()).thenReturn("test-token");
        when(http.newWebSocketBuilder()).thenReturn(builder);
        when(builder.connectTimeout(any())).thenReturn(builder);
        when(builder.buildAsync(any(URI.class), any(WebSocket.Listener.class))).thenAnswer(invocation -> {
            Connection connection = new Connection(invocation.getArgument(1));
            connections.add(connection);
            connection.listener.onOpen(connection.socket);
            if (helloBeforeConnectCompletes) {
                connection.receive("{\"op\":10,\"d\":{\"heartbeat_interval\":18000}}");
            }
            return CompletableFuture.completedFuture(connection.socket);
        });
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(scheduler).execute(any(Runnable.class));
        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            Task task = new Task(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2));
            delayed.add(task);
            return task.future;
        });
        when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> {
                    Task task = new Task(invocation.getArgument(0), invocation.getArgument(2), invocation.getArgument(3));
                    heartbeats.add(task);
                    return task.future;
                });
        QQBotConfig config = QQBotConfig.builder().appId("test-app").appSecret("test-secret").build();
        client = new GatewayClient(config, tokens, api::gatewayUrl, dispatcher, http, scheduler, now::get, true);
        client.start();
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    private Connection current() {
        return connections.get(connections.size() - 1);
    }

    private Task lastDelayed() {
        return delayed.get(delayed.size() - 1);
    }

    private void ready() {
        current().receive("{\"op\":10,\"d\":{\"heartbeat_interval\":18000}}");
        current().receive("{\"op\":0,\"s\":10,\"t\":\"READY\",\"d\":{\"session_id\":\"session-1\"}}");
    }

    private JSONObject reconnectAndHello() {
        lastDelayed().action.run();
        current().receive("{\"op\":10,\"d\":{\"heartbeat_interval\":18000}}");
        return current().lastSent();
    }

    @Test
    void managementSnapshotTracksConnectionWithoutUsingBusinessSilenceAsOffline() {
        assertEquals("CONNECTING",client.managementSnapshot().phase());ready();
        assertEquals("READY",client.managementSnapshot().phase());assertEquals(now.get(),client.managementSnapshot().lastReadyAt());assertNull(client.managementSnapshot().lastEventAt());
        now.addAndGet(3_600_000);assertEquals("READY",client.managementSnapshot().phase());
        current().receive("{\"op\":7}");assertEquals("RECONNECTING",client.managementSnapshot().phase());
        reconnectAndHello();current().receive("{\"op\":0,\"s\":12,\"t\":\"RESUMED\",\"d\":{}}");assertEquals("READY",client.managementSnapshot().phase());assertEquals(now.get(),client.managementSnapshot().lastReadyAt());
        String snapshot=JSON.toJSONString(client.managementSnapshot());assertFalse(snapshot.contains("session-1"));assertFalse(snapshot.contains("test-token"));
    }

    @Test
    void debugLoggingOmitsRawFramesButKeepsReadyAndDispatchesEvents() {
        Logger gatewayLogger = (Logger) LoggerFactory.getLogger(top.sshh.qqbot.sdk.GatewayClient.class);
        Level previousLevel = gatewayLogger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.setContext(gatewayLogger.getLoggerContext());
        appender.start();
        gatewayLogger.addAppender(appender);
        gatewayLogger.setLevel(Level.DEBUG);
        try {
            ready();
            assertTrue(appender.list.stream().anyMatch(event ->
                    event.getFormattedMessage().contains("官方网关 READY，机器人上线")));
            assertTrue(appender.list.stream().noneMatch(event ->
                    event.getFormattedMessage().contains("session-1")));

            appender.list.clear();
            current().receive("{\"op\":11}");
            assertTrue(appender.list.isEmpty(), "心跳 ACK 不应产生原始报文日志");

            JSONObject body = JSONObject.of("id", "event-private", "content", "private group message",
                    "message_scene", JSONObject.of("auth_token", "private-event-token"));
            String payload = JSONObject.of("op", 0, "s", 11, "t", "GROUP_MESSAGE_CREATE", "d", body).toJSONString();
            current().receive(payload);
            verify(dispatcher).onEvent(argThat(event -> event.type().equals("GROUP_MESSAGE_CREATE") && JSON.parseObject(event.dataJson()).equals(body)));
            assertTrue(appender.list.stream().noneMatch(event -> {
                String message = event.getFormattedMessage();
                return message.contains("网关原始消息") || message.contains("private group message")
                        || message.contains("private-event-token");
            }));
        } finally {
            gatewayLogger.setLevel(previousLevel);
            gatewayLogger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void firstConnectionIdentifiesAndStableOp7ResumesAfterOneSecond() {
        ready();
        assertEquals(2, current().lastSent().getIntValue("op"));
        current().receive("{\"op\":0,\"s\":42,\"t\":\"GROUP_AT_MESSAGE_CREATE\",\"d\":{\"id\":\"event-42\"}}");
        now.addAndGet(3_600_000);
        Connection old = current();
        old.receive("{\"op\":7}");
        verify(old.socket).abort();
        assertEquals(1000, lastDelayed().delayMs);
        JSONObject resume = reconnectAndHello();
        assertEquals(6, resume.getIntValue("op"));
        assertEquals("QQBot test-token", resume.getJSONObject("d").getString("token"));
        assertEquals("session-1", resume.getJSONObject("d").getString("session_id"));
        assertEquals(42, resume.getJSONObject("d").getLongValue("seq"));
        assertFalse(resume.getJSONObject("d").containsKey("intents"));
    }

    @Test
    void helloBeforeBuildAsyncCompletesCanResumeUsingListenerSocket() {
        ready();
        helloBeforeConnectCompletes = true;
        current().receive("{\"op\":7}");
        lastDelayed().action.run();
        assertEquals(6, current().lastSent().getIntValue("op"));
        assertEquals("session-1", current().lastSent().getJSONObject("d").getString("session_id"));
    }

    @Test
    void failedFastReconnectFallsBackToNormalDelay() {
        ready();
        now.addAndGet(3_600_000);
        current().receive("{\"op\":7}");
        assertEquals(1000, lastDelayed().delayMs);
        when(api.gatewayUrl()).thenReturn(null);
        lastDelayed().action.run();
        assertEquals(5000, lastDelayed().delayMs);
        lastDelayed().action.run();
        assertEquals(10_000, lastDelayed().delayMs);
    }

    @Test
    void replayedEventsAreDispatchedBeforeResumedAndStableSuccessResetsBackoff() {
        ready();
        current().listener.onError(current().socket, new IllegalStateException("network"));
        assertEquals(5000, lastDelayed().delayMs);
        reconnectAndHello();
        current().receive("{\"op\":0,\"s\":11,\"t\":\"GROUP_AT_MESSAGE_CREATE\",\"d\":{\"id\":\"replayed\"}}");
        verify(dispatcher).onEvent(argThat(event -> event.type().equals("GROUP_AT_MESSAGE_CREATE") && "replayed".equals(event.data().get("id"))));
        Task authenticationTimeout = lastDelayed();
        current().receive("{\"op\":0,\"s\":12,\"t\":\"RESUMED\",\"d\":\"\"}");
        assertTrue(authenticationTimeout.cancelled);
        now.addAndGet(60_000);
        current().listener.onError(current().socket, new IllegalStateException("network"));
        assertEquals(5000, lastDelayed().delayMs);
        assertEquals(12, reconnectAndHello().getJSONObject("d").getLongValue("seq"));
    }

    @Test
    void invalidSessionFallsBackToIdentifyAndClearsOldSequence() {
        ready();
        current().receive("{\"op\":7}");
        assertEquals(6, reconnectAndHello().getIntValue("op"));
        current().receive("{\"op\":9,\"d\":false}");
        JSONObject identify = reconnectAndHello();
        assertEquals(2, identify.getIntValue("op"));
        assertFalse(identify.getJSONObject("d").containsKey("session_id"));
        current().receive("{\"op\":1}");
        JSONObject heartbeat = current().lastSent();
        assertEquals(1, heartbeat.getIntValue("op"));
        assertTrue(heartbeat.containsKey("d"), "首个心跳需要在报文中明确携带 d:null");
        assertNull(heartbeat.get("d"));
    }

    @Test
    void recoverableInvalidSessionRetriesResumeOnceThenFallsBack() {
        ready();
        current().receive("{\"op\":7}");
        reconnectAndHello();
        current().receive("{\"op\":9,\"d\":true}");
        assertEquals(6, reconnectAndHello().getIntValue("op"));
        current().receive("{\"op\":9,\"d\":true}");
        assertEquals(2, reconnectAndHello().getIntValue("op"));
    }

    @Test
    void resumeTimeoutFallsBackEvenWhenHeartbeatAcksContinue() {
        ready();
        current().receive("{\"op\":7}");
        reconnectAndHello();
        current().receive("{\"op\":11}");
        lastDelayed().action.run();
        assertEquals(2, reconnectAndHello().getIntValue("op"));
    }

    @Test
    void freshSessionWithoutIdIdentifiesAgain() {
        current().receive("{\"op\":10}");
        current().receive("{\"op\":0,\"s\":1,\"t\":\"READY\",\"d\":{}}");
        current().receive("{\"op\":7}");
        assertEquals(2, reconnectAndHello().getIntValue("op"));
    }

    @Test
    void oldFramesErrorsAndTimersCannotChangeNewConnection() {
        ready();
        Task oldHelloTimeout = delayed.get(0);
        Task oldAuthenticationTimeout = lastDelayed();
        Task oldHeartbeat = heartbeats.get(0);
        Connection old = current();
        old.receive("{\"op\":7}");
        assertTrue(oldHelloTimeout.cancelled);
        assertTrue(oldAuthenticationTimeout.cancelled);
        assertTrue(oldHeartbeat.cancelled);
        reconnectAndHello();
        int tasksBefore = delayed.size();
        old.listener.onClose(old.socket, 4007, "stale close");
        old.listener.onError(old.socket, new IllegalStateException("stale error"));
        old.receive("{\"op\":9,\"d\":false}");
        old.receive("{\"op\":0,\"s\":999,\"t\":\"READY\",\"d\":{\"session_id\":\"stale\"}}");
        oldHelloTimeout.action.run();
        oldAuthenticationTimeout.action.run();
        oldHeartbeat.action.run();
        assertEquals(tasksBefore, delayed.size());
        assertEquals(1, current().sent.size());
        verify(current().socket, never()).abort();
        current().receive("{\"op\":7}");
        assertEquals("session-1", reconnectAndHello().getJSONObject("d").getString("session_id"));
    }

    @Test
    void newConnectionMustReceiveItsOwnHello() {
        ready();
        current().receive("{\"op\":7}");
        lastDelayed().action.run();
        Connection fresh = current();
        lastDelayed().action.run();
        verify(fresh.socket).abort();
        assertEquals(10_000, lastDelayed().delayMs);
    }

    @Test
    void heartbeatUsesNewSessionBaselineAndReconnectsWhenNoAckArrives() {
        ready();
        current().receive("{\"op\":11}");
        now.addAndGet(3_600_000);
        current().receive("{\"op\":7}");
        reconnectAndHello();
        current().receive("{\"op\":0,\"s\":11,\"t\":\"RESUMED\",\"d\":\"\"}");
        now.addAndGet(40_000);
        Task heartbeat = heartbeats.get(heartbeats.size() - 1);
        heartbeat.action.run();
        verify(current().socket, never()).abort();
        now.addAndGet(1001);
        heartbeat.action.run();
        verify(current().socket).abort();
    }

    @Test
    void repeatedShortLivedOp7ConnectionsUseExponentialBackoff() {
        ready();
        current().receive("{\"op\":7}");
        assertEquals(5000, lastDelayed().delayMs);
        reconnectAndHello();
        current().receive("{\"op\":7}");
        assertEquals(10_000, lastDelayed().delayMs);
        reconnectAndHello();
        current().receive("{\"op\":7}");
        assertEquals(20_000, lastDelayed().delayMs);
    }

    @Test
    void repeatedShortLivedResumedConnectionsDoNotResetBackoff() {
        ready();
        current().receive("{\"op\":7}");
        assertEquals(5000, lastDelayed().delayMs);
        reconnectAndHello();
        current().receive("{\"op\":0,\"s\":11,\"t\":\"RESUMED\",\"d\":\"\"}");
        current().receive("{\"op\":7}");
        assertEquals(10_000, lastDelayed().delayMs);
        reconnectAndHello();
        current().receive("{\"op\":0,\"s\":12,\"t\":\"RESUMED\",\"d\":\"\"}");
        current().receive("{\"op\":7}");
        assertEquals(20_000, lastDelayed().delayMs);
    }

    @Test
    void fragmentedPayloadFromRetiredSocketDoesNotPolluteNewSocket() {
        ready();
        Connection old = current();
        old.listener.onText(old.socket, "{\"op\":", false);
        old.listener.onError(old.socket, new IllegalStateException("network"));
        reconnectAndHello();
        old.listener.onText(old.socket, "9}", true);
        current().receive("{\"op\":1}");
        assertEquals(1, current().lastSent().getIntValue("op"));
        assertEquals(10, current().lastSent().getLongValue("d"));
    }

    @Test
    void invalidSequenceCloseForcesFreshIdentify() {
        ready();
        current().listener.onClose(current().socket, 4007, "invalid seq");
        assertEquals(2, reconnectAndHello().getIntValue("op"));
    }

    @Test
    void invalidTokenCloseClearsTokenCacheButKeepsSessionForResume() {
        ready();
        current().listener.onClose(current().socket, 4004, "invalid token");
        verify(tokens).invalidateToken();
        when(tokens.getToken()).thenReturn("fresh-token");
        JSONObject resume = reconnectAndHello();
        assertEquals(6, resume.getIntValue("op"));
        assertEquals("QQBot fresh-token", resume.getJSONObject("d").getString("token"));
    }

    @Test
    void failedDispatchDoesNotAdvanceResumeSequence() {
        ready();
        doThrow(new IllegalStateException("worker unavailable")).when(dispatcher).onEvent(any());
        current().receive("{\"op\":0,\"s\":11,\"t\":\"GROUP_AT_MESSAGE_CREATE\",\"d\":{}}");
        assertEquals(10, reconnectAndHello().getJSONObject("d").getLongValue("seq"));
    }

    @Test
    void asynchronousSendFailureReconnectsOnlyOnce() {
        ready();
        Connection connection = current();
        when(connection.socket.sendText(any(), eq(true)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("send failed")));
        connection.receive("{\"op\":1}");
        int tasksAfter = delayed.size();
        connection.listener.onError(connection.socket, new IllegalStateException("same failure"));
        connection.listener.onClose(connection.socket, 1006, "same failure");
        assertEquals(tasksAfter, delayed.size());
        verify(connection.socket, times(1)).abort();
    }

    @Test
    void overlappingHeartbeatsWaitForPreviousSendToComplete() {
        ready();
        Connection connection = current();
        CompletableFuture<WebSocket> pending = new CompletableFuture<>();
        AtomicInteger sends = new AtomicInteger();
        when(connection.socket.sendText(any(), eq(true))).thenAnswer(invocation -> {
            connection.sent.add(JSON.parseObject(invocation.<CharSequence>getArgument(0).toString()));
            return sends.getAndIncrement() == 0 ? pending : CompletableFuture.completedFuture(connection.socket);
        });
        connection.receive("{\"op\":1}");
        heartbeats.get(0).action.run();
        assertEquals(1, sends.get());
        pending.complete(connection.socket);
        assertEquals(2, sends.get());
        verify(connection.socket, never()).abort();
        assertEquals(1, connection.lastSent().getIntValue("op"));
    }

    @Test
    void queuedHeartbeatIsNotSentAfterSocketHasBeenRetired() {
        ready();
        Connection old = current();
        CompletableFuture<WebSocket> pending = new CompletableFuture<>();
        when(old.socket.sendText(any(), eq(true))).thenReturn(pending);
        old.receive("{\"op\":1}");
        old.receive("{\"op\":1}");
        old.receive("{\"op\":7}");
        reconnectAndHello();
        pending.complete(old.socket);
        // Identify + 一个已开始的心跳；排队的第二个心跳不得再发到旧 socket。
        verify(old.socket, times(2)).sendText(any(), eq(true));
        verify(current().socket, never()).abort();
    }

    @Test
    void stoppedClientIgnoresQueuedReconnectAndCallbacks() {
        ready();
        Connection old = current();
        old.receive("{\"op\":7}");
        Task queuedReconnect = lastDelayed();
        client.close();
        queuedReconnect.action.run();
        old.listener.onError(old.socket, new IllegalStateException("after shutdown"));
        assertEquals(1, connections.size());
        verify(scheduler).shutdownNow();
    }

    @Test void fatalCloseStopsReconnectsAndInvokesHandlerOutsideTheGatewayLock() {
        ready();
        List<QQApiException> failures = new ArrayList<>();
        client.onFatalError(error -> {
            assertFalse(Thread.holdsLock(client)); failures.add(error);
        });
        int tasks = delayed.size();
        current().listener.onClose(current().socket, 4915, "banned");
        assertEquals("FAILED", client.managementSnapshot().phase());
        assertEquals(1, failures.size()); assertEquals(4915L, failures.get(0).platformCode());
        assertEquals(tasks, delayed.size());
    }
    @Test void rateLimitedGatewayWaitsAtLeastSixtySeconds() {
        ready(); now.addAndGet(60_000);
        current().listener.onClose(current().socket, 4008, "rate limited");
        assertEquals(60_000, lastDelayed().delayMs);
    }
    @Test void repeatedStartDoesNotCreateAnotherSocketAndClosedClientsCannotRestart() {
        client.start(); assertEquals(1, connections.size());
        client.close(); assertThrows(IllegalStateException.class, client::start);
    }
    @Test void callerOwnedSchedulersAreNotShutdown() {
        ScheduledExecutorService external = mock(ScheduledExecutorService.class);
        GatewayClient standalone = new GatewayClient(QQBotConfig.builder().build(), tokens, api::gatewayUrl,
                dispatcher, mock(HttpClient.class), external, now::get, false);
        standalone.close(); verify(external, never()).shutdownNow();
    }
    @Test void configuredShardAndIntentsAreSentInIdentify() {
        var config = QQBotConfig.builder().intents(1 << 30).shard(2, 4).build();
        try (GatewayClient shard = new GatewayClient(config, tokens, api::gatewayUrl, dispatcher, http, scheduler, now::get, false)) {
            shard.start(); current().receive("{\"op\":10,\"d\":{\"heartbeat_interval\":18000}}");
            JSONObject identify = current().lastSent().getJSONObject("d");
            assertEquals(1 << 30, identify.getIntValue("intents"));
            assertEquals(List.of(2, 4), identify.getJSONArray("shard"));
        }
        assertThrows(IllegalArgumentException.class, () -> QQBotConfig.builder().shard(4, 4).build());
    }

    private static class Task {
        final Runnable action;
        final long delayMs;
        final ScheduledFuture<?> future = mock(ScheduledFuture.class);
        boolean cancelled;

        Task(Runnable action, long delay, TimeUnit unit) {
            this.action = action;
            this.delayMs = unit.toMillis(delay);
            doAnswer(invocation -> {
                cancelled = true;
                return true;
            }).when(future).cancel(false);
        }
    }

    private static class Connection {
        final WebSocket.Listener listener;
        final WebSocket socket = mock(WebSocket.class);
        final List<JSONObject> sent = new ArrayList<>();

        Connection(WebSocket.Listener listener) {
            this.listener = listener;
            when(socket.sendText(any(), eq(true))).thenAnswer(invocation -> {
                sent.add(JSON.parseObject(invocation.<CharSequence>getArgument(0).toString()));
                return CompletableFuture.completedFuture(socket);
            });
        }

        void receive(String payload) {
            listener.onText(socket, payload, true);
        }

        JSONObject lastSent() {
            return sent.get(sent.size() - 1);
        }
    }
}
