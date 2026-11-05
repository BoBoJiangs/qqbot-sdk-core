package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/**
 * QQ 开放平台 WebSocket 网关客户端。
 * Hello → Identify → READY；断线保留 session_id/seq，优先 Resume 补收遗漏事件。
 * 服务端 op7 在连接稳定后快速恢复；网络故障退避重连，会话失效时重新 Identify。
 * 每条连接独立维护接收缓冲和定时任务，旧连接回调不得干扰新连接。
 */
public final class GatewayClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GatewayClient.class);
    private static final int OP_DISPATCH = 0;
    private static final int OP_HEARTBEAT = 1;
    private static final int OP_IDENTIFY = 2;
    private static final int OP_RESUME = 6;
    private static final int OP_RECONNECT = 7;
    private static final int OP_INVALID_SESSION = 9;
    private static final int OP_HELLO = 10;
    private static final int OP_HEARTBEAT_ACK = 11;
    private static final long STABLE_CONNECTION_MS = 60_000;

    private final QQBotConfig config;
    private final java.util.function.Supplier<String> gatewayUrl;
    private final AccessTokenProvider tokenManager;
    private final BotEventHandler handler;
    private final boolean ownsScheduler;
    private boolean closed;
    private int minimumReconnectDelaySec;
    private QQApiException fatalFailure;
    private java.util.function.Consumer<QQApiException> fatalHandler = ignored -> {};
    private final HttpClient http;
    private final ScheduledExecutorService scheduler;
    private final LongSupplier clock;
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);

    private volatile GatewayListener activeConnection;
    private volatile boolean running;
    private String sessionId;
    private long lastSeq = -1;
    private int reconnectDelaySec = 5;
    private int resumeRejections;
    private volatile long lastReadyAt;
    private volatile long lastEventAt;
    private volatile long lastHeartbeatAckAt;

    public record StatusSnapshot(String phase, Long lastReadyAt, Long lastHeartbeatAckAt, Long lastEventAt, long sampledAt) {}
    /** Explicit allowlist: never expose session IDs, sequence numbers, tokens or raw events. */
    public synchronized StatusSnapshot managementSnapshot() {
        String phase=fatalFailure != null ? "FAILED" : !running?"STOPPED":activeConnection!=null&&activeConnection.authenticated?"READY":reconnectScheduled.get()?"RECONNECTING":"CONNECTING";
        return new StatusSnapshot(phase,lastReadyAt==0?null:lastReadyAt,lastHeartbeatAckAt==0?null:lastHeartbeatAckAt,lastEventAt==0?null:lastEventAt,clock.getAsLong());
    }

    public GatewayClient(QQBotConfig config, AccessTokenProvider tokenManager,
                         java.util.function.Supplier<String> gatewayUrl, BotEventHandler handler, HttpClient http) {
        this(config, tokenManager, gatewayUrl, handler, http,
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "qqbot-gateway"); t.setDaemon(true); return t;
                }), System::currentTimeMillis, true);
    }

    /** The caller retains ownership of injected schedulers unless ownsScheduler is true. */
    public GatewayClient(QQBotConfig config, AccessTokenProvider tokenManager,
                         java.util.function.Supplier<String> gatewayUrl, BotEventHandler handler, HttpClient http,
                         ScheduledExecutorService scheduler, LongSupplier clock, boolean ownsScheduler) {
        this.config = java.util.Objects.requireNonNull(config);
        this.tokenManager = java.util.Objects.requireNonNull(tokenManager);
        this.gatewayUrl = java.util.Objects.requireNonNull(gatewayUrl);
        this.handler = java.util.Objects.requireNonNull(handler);
        this.http = java.util.Objects.requireNonNull(http);
        this.scheduler = java.util.Objects.requireNonNull(scheduler);
        this.clock = java.util.Objects.requireNonNull(clock);
        this.ownsScheduler = ownsScheduler;
    }

    public synchronized GatewayClient onFatalError(java.util.function.Consumer<QQApiException> handler) {
        fatalHandler = java.util.Objects.requireNonNull(handler); return this;
    }

    public synchronized void start() {
        if (closed) throw new IllegalStateException("Gateway is closed");
        if (running) return;
        fatalFailure = null;
        running = true;
        scheduler.execute(this::connect);
    }

    private void connect() {
        if (!running || !connecting.compareAndSet(false, true)) {
            return;
        }
        GatewayListener connection = null;
        try {
            String gatewayUrl = this.gatewayUrl.get();
            if (gatewayUrl == null || gatewayUrl.isBlank()) {
                scheduleReconnect(false);
                return;
            }
            synchronized (this) {
                if (!running) {
                    return;
                }
                connection = new GatewayListener();
                activeConnection = connection;
            }
            logger.info("连接官方网关: {}", gatewayUrl);
            // socket 由 onOpen 发布：Hello 可能在 buildAsync().join() 返回前到达。
            http.newWebSocketBuilder().connectTimeout(config.timeout())
                    .buildAsync(URI.create(gatewayUrl), connection).join();
        } catch (Exception e) {
            logger.error("连接官方网关失败: {}", e.getMessage());
            if (connection != null) {
                reconnect(connection, false);
            } else {
                scheduleReconnect(false);
            }
        } finally {
            connecting.set(false);
        }
    }

    private synchronized void scheduleReconnect(boolean fast) {
        if (!running || !reconnectScheduled.compareAndSet(false, true)) {
            return;
        }
        int delay = Math.max(fast ? 1 : reconnectDelaySec, minimumReconnectDelaySec);
        minimumReconnectDelaySec = 0;
        logger.info("{}秒后重连官方网关（{}）...", delay, canResume() ? "恢复会话" : "重新登录");
        scheduler.schedule(() -> {
            reconnectScheduled.set(false);
            connect();
        }, delay, TimeUnit.SECONDS);
        if (!fast) {
            reconnectDelaySec = Math.min(reconnectDelaySec * 2, 60);
        }
    }

    private boolean canResume() {
        return sessionId != null && !sessionId.isBlank() && lastSeq >= 0;
    }

    private void clearSession() {
        sessionId = null;
        lastSeq = -1;
        resumeRejections = 0;
    }

    private void authenticate(GatewayListener connection) {
        String token = tokenManager.getToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("网关鉴权没有可用 token");
        }
        JSONObject d = new JSONObject();
        d.put("token", "QQBot " + token);
        connection.resumeAttempt = canResume();
        if (connection.resumeAttempt) {
            d.put("session_id", sessionId);
            d.put("seq", lastSeq);
        } else {
            clearSession();
            d.put("intents", config.intents());
            JSONArray shard = new JSONArray();
            shard.add(config.shardId());
            shard.add(config.shardCount());
            d.put("shard", shard);
        }
        JSONObject payload = new JSONObject();
        payload.put("op", connection.resumeAttempt ? OP_RESUME : OP_IDENTIFY);
        payload.put("d", d);
        String action = connection.resumeAttempt ? "Resume" : "Identify";
        send(connection, payload, action);
        logger.info("已提交 {}（seq={} intents={}）", action, lastSeq, config.intents());
    }

    private void send(GatewayListener connection, JSONObject payload, String action) {
        // JDK 不允许同时存在两次未完成的 sendText；服务端请求心跳可能与定时心跳重叠。
        String text = payload.toJSONString(JSONWriter.Feature.WriteNulls);
        connection.sendTail = connection.sendTail.thenCompose(ignored -> {
            synchronized (this) {
                if (!isActive(connection)) {
                    return CompletableFuture.completedFuture(connection.socket);
                }
                return connection.socket.sendText(text, true);
            }
        });
        connection.sendTail.whenComplete((v, err) -> {
            synchronized (this) {
                if (err != null && isActive(connection)) {
                    logger.warn("{}发送失败: {}", action, err.toString());
                    reconnect(connection, false);
                }
            }
        });
    }

    private synchronized void heartbeat(GatewayListener connection) {
        if (!isActive(connection) || !connection.helloReceived) {
            return;
        }
        long now = clock.getAsLong();
        if (isHeartbeatAckTimedOut(connection.lastAckAt, connection.helloAt,
                now, connection.heartbeatIntervalMs)) {
            logger.warn("心跳应答超时（{}ms 未收到 op11），强制重连",
                    now - Math.max(connection.lastAckAt, connection.helloAt));
            reconnect(connection, false);
            return;
        }
        JSONObject hb = new JSONObject();
        hb.put("op", OP_HEARTBEAT);
        hb.put("d", lastSeq < 0 ? null : lastSeq);
        send(connection, hb, "心跳");
    }

    private boolean isActive(GatewayListener connection) {
        return running && activeConnection == connection;
    }

    private synchronized void reconnect(GatewayListener connection, boolean fast) {
        if (!isActive(connection)) {
            return;
        }
        boolean stable = connection.authenticated
                && clock.getAsLong() - connection.openedAt >= STABLE_CONNECTION_MS;
        // 仅稳定连接重置退避；READY/RESUMED 后立刻被踢仍累积失败等待时间。
        if (stable) {
            reconnectDelaySec = 5;
        }
        // 先退役，再 abort：可能迟到的 onClose/onError 不得调度第二次重连。
        activeConnection = null;
        connection.cancelTasks();
        if (connection.socket != null) {
            connection.socket.abort();
        }
        scheduleReconnect(fast && stable);
    }

    private class GatewayListener implements WebSocket.Listener {
        private final StringBuilder textBuffer = new StringBuilder();
        private WebSocket socket;
        private CompletableFuture<WebSocket> sendTail = CompletableFuture.completedFuture(null);
        private long openedAt;
        private long helloAt;
        private long lastAckAt;
        private long heartbeatIntervalMs = 30_000;
        private boolean helloReceived;
        private boolean authenticated;
        private boolean resumeAttempt;
        private ScheduledFuture<?> helloTimeout;
        private ScheduledFuture<?> authenticationTimeout;
        private ScheduledFuture<?> heartbeatTask;

        private void cancelTasks() {
            if (helloTimeout != null) helloTimeout.cancel(false);
            if (authenticationTimeout != null) authenticationTimeout.cancel(false);
            if (heartbeatTask != null) heartbeatTask.cancel(false);
        }

        @Override
        public void onOpen(WebSocket ws) {
            synchronized (GatewayClient.this) {
                if (!isActive(this)) {
                    ws.abort();
                    return;
                }
                socket = ws;
                openedAt = clock.getAsLong();
                logger.info("官方网关WS连接已建立");
                helloTimeout = scheduler.schedule(() -> {
                    synchronized (GatewayClient.this) {
                        if (isActive(this) && !helloReceived) {
                            logger.warn("30秒未收到网关Hello，主动断开重连");
                            reconnect(this, false);
                        }
                    }
                }, 30, TimeUnit.SECONDS);
                // JDK WebSocket 初始 demand=0，必须主动 request。
                ws.request(1);
            }
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            synchronized (GatewayClient.this) {
                if (!isActive(this)) {
                    return null;
                }
                if (textBuffer.length() + data.length() > 8 * 1024 * 1024) {
                    reconnect(this, false); return null;
                }
                textBuffer.append(data);
                if (last) {
                    String payload = textBuffer.toString();
                    textBuffer.setLength(0);
                    handlePayload(payload, this);
                }
                if (isActive(this)) ws.request(1);
                return null;
            }
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            QQApiException failure = null;
            synchronized (GatewayClient.this) {
                if (!isActive(this)) return null;
                logger.warn("官方网关连接关闭: code={} reason={}", statusCode, reason);
                if (statusCode == 4914 || statusCode == 4915) {
                    activeConnection = null; running = false; cancelTasks(); ws.abort();
                    fatalFailure = new QQApiException(QQApiException.Kind.GATEWAY_FATAL, 0, (long) statusCode, null, null);
                    failure = fatalFailure;
                } else {
                    if (statusCode == 4008) minimumReconnectDelaySec = 60;
                    if (statusCode == 4004) tokenManager.invalidateToken();
                    else if (statusCode == 4006 || statusCode == 4007 || statusCode == 4009
                        || (statusCode >= 4900 && statusCode <= 4913)) {
                        clearSession();
                    }
                    reconnect(this, false);
                }
            }
            if (failure != null) {
                try { fatalHandler.accept(failure); }
                catch (RuntimeException ignored) { logger.warn("Gateway fatal error handler failed"); }
            }
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            synchronized (GatewayClient.this) {
                if (!isActive(this)) return;
                logger.warn("官方网关连接异常: {}", error.getMessage());
                reconnect(this, false);
            }
        }
    }

    private void handlePayload(String payload, GatewayListener connection) {
        try {
            JSONObject msg = JSON.parseObject(payload);
            int op = msg.getIntValue("op");
            if (op == OP_HELLO) {
                if (connection.helloReceived) return;
                connection.helloReceived = true;
                connection.helloAt = clock.getAsLong();
                connection.helloTimeout.cancel(false);
                JSONObject d = msg.getJSONObject("d");
                long interval = d == null ? 30_000 : d.getLongValue("heartbeat_interval");
                connection.heartbeatIntervalMs = interval > 0 ? interval : 30_000;
                connection.heartbeatTask = scheduler.scheduleAtFixedRate(() -> heartbeat(connection),
                        connection.heartbeatIntervalMs, connection.heartbeatIntervalMs, TimeUnit.MILLISECONDS);
                connection.authenticationTimeout = scheduler.schedule(() -> {
                    synchronized (this) {
                        if (isActive(connection) && !connection.authenticated) {
                            logger.warn("30秒未收到 {}，主动断开重连",
                                    connection.resumeAttempt ? "RESUMED" : "READY");
                            if (connection.resumeAttempt) clearSession();
                            reconnect(connection, false);
                        }
                    }
                }, 30, TimeUnit.SECONDS);
                authenticate(connection);
            } else if (op == OP_DISPATCH) {
                String type = msg.getString("t");
                Long seq = msg.getLong("s");
                if ("READY".equals(type)) {
                    JSONObject d = msg.getJSONObject("d");
                    sessionId = d == null ? null : d.getString("session_id");
                    lastSeq = seq == null ? -1 : seq;
                    connection.authenticated = true;
                    lastReadyAt = clock.getAsLong();
                    connection.authenticationTimeout.cancel(false);
                    resumeRejections = 0;
                    logger.info("官方网关 READY，机器人上线（可恢复会话={}）", canResume());
                } else if ("RESUMED".equals(type)) {
                    lastReadyAt = clock.getAsLong();
                    if (seq != null) lastSeq = seq;
                    connection.authenticated = true;
                    connection.authenticationTimeout.cancel(false);
                    resumeRejections = 0;
                    logger.info("官方网关 RESUMED，会话恢复完成（seq={}）", lastSeq);
                } else if (type != null) {
                    // dispatch 成功交给业务线程后再更新 seq，失败时保留旧序号供 Resume 补发。
                    handler.onEvent(new BotEvent(type, msg.getString("id"), seq,
                            msg.getJSONObject("d") == null ? "{}" : msg.getJSONObject("d").toJSONString(JSONWriter.Feature.WriteNulls)));
                    lastEventAt = clock.getAsLong();
                    if (seq != null) lastSeq = seq;
                }
            } else if (op == OP_RECONNECT) {
                long aliveMs = clock.getAsLong() - connection.openedAt;
                long ackAgeMs = connection.lastAckAt == 0 ? -1
                        : clock.getAsLong() - connection.lastAckAt;
                logger.info("服务端要求重连（op7，连接存活={}ms ACK距今={}ms，可恢复会话={}）",
                        aliveMs, ackAgeMs, canResume());
                reconnect(connection, true);
            } else if (op == OP_INVALID_SESSION) {
                boolean recoverable = Boolean.TRUE.equals(msg.get("d"));
                // 可恢复的 op9 最多重试一次；防止无效 session 永久陷入 Resume 循环。
                if (!recoverable || !canResume() || ++resumeRejections >= 2) {
                    clearSession();
                }
                logger.warn("会话失效（op9），稍后{}", canResume() ? "重试 Resume" : "重新 Identify");
                reconnect(connection, false);
            } else if (op == OP_HEARTBEAT_ACK) {
                connection.lastAckAt = clock.getAsLong();
                lastHeartbeatAckAt = connection.lastAckAt;
            } else if (op == OP_HEARTBEAT) {
                heartbeat(connection);
            }
        } catch (Exception e) {
            logger.error("处理网关消息异常: {}", e.getClass().getSimpleName());
            reconnect(connection, false);
        }
    }

    public static boolean isHeartbeatAckTimedOut(long lastAckAt, long sessionStartedAt,
                                          long now, long intervalMs) {
        long referenceAt = Math.max(lastAckAt, sessionStartedAt);
        return referenceAt > 0 && now - referenceAt > 2 * intervalMs + 5000;
    }

    public synchronized void close() {
        if (closed) return;
        closed = true;
        running = false;
        GatewayListener connection = activeConnection;
        activeConnection = null;
        if (connection != null) {
            connection.cancelTasks();
            if (connection.socket != null) connection.socket.abort();
        }
        if (ownsScheduler) scheduler.shutdownNow();
    }
}
