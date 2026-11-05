package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Standalone facade. Network work starts only with start() or an explicit API call. */
public final class QQBot implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(QQBot.class);
    private final QQBotConfig config;
    private final ApiClient api;
    private final QQOpenApi openApi;
    private final GatewayClient gateway;
    private final ExecutorService events;
    private final boolean ownsEvents;
    private final HttpClient http;
    private final CountDownLatch terminated = new CountDownLatch(1);
    private final List<Consumer<BotEvent>> listeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Throwable>> errors = new CopyOnWriteArrayList<>();
    private final Map<String, Long> seen = new LinkedHashMap<>();
    private volatile boolean closed;
    private volatile boolean failed;

    private QQBot(Builder b) {
        config = Objects.requireNonNull(b.config);
        if (config.appId().isBlank() || config.appSecret().isBlank()) throw new IllegalArgumentException("AppID and AppSecret are required");
        http = b.http == null ? HttpClient.newBuilder().connectTimeout(config.timeout()).build() : b.http;
        ownsEvents = b.executor == null;
        events = ownsEvents ? new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(b.queueCapacity), r -> {
                    Thread t = new Thread(r, "qqbot-events"); t.setDaemon(true); return t;
                }, new ThreadPoolExecutor.AbortPolicy()) : b.executor;
        AccessTokenProvider tokens = b.tokens == null ? new TokenManager(config, http) : b.tokens;
        api = new ApiClient(config, tokens, http); openApi = new QQOpenApi(api);
        gateway = new GatewayClient(config, tokens, api::gatewayUrl, this::accept, http)
                .onFatalError(error -> { failed = true; report(error); terminated.countDown(); });
    }
    public static Builder builder(QQBotConfig config) { return new Builder(config); }
    public ApiClient api() { return api; }
    public QQOpenApi openApi() { return openApi; }
    public GatewayClient gateway() { return gateway; }
    public MediaUploader mediaUploader() { return new MediaUploader(http, config.timeout()); }
    public C2CStream stream(String userId, String replyMessageId) { return new C2CStream(openApi, userId, replyMessageId); }
    public QQBot onEvent(Consumer<BotEvent> listener) { listeners.add(Objects.requireNonNull(listener)); return this; }
    public QQBot on(EventType type, Consumer<BotEvent> listener) {
        Objects.requireNonNull(listener); return onEvent(event -> { if (event.type().equals(type.name())) listener.accept(event); });
    }
    public QQBot onGroupMessage(Consumer<MessageEvent> listener) {
        Objects.requireNonNull(listener);
        return onEvent(event -> {
            if (event.type().equals("GROUP_AT_MESSAGE_CREATE") || event.type().equals("GROUP_MESSAGE_CREATE")) listener.accept(MessageEvent.from(event));
        });
    }
    public QQBot onC2CMessage(Consumer<MessageEvent> listener) {
        Objects.requireNonNull(listener);
        return onEvent(event -> { if (event.type().equals("C2C_MESSAGE_CREATE")) listener.accept(MessageEvent.from(event)); });
    }
    public QQBot onError(Consumer<Throwable> listener) { errors.add(Objects.requireNonNull(listener)); return this; }
    public ApiResponse reply(MessageEvent event, Message message) { return api.send(event.target(), message, event.id()); }
    public void start() { if (closed || failed) throw new IllegalStateException("Bot is closed or failed"); gateway.start(); }
    public void awaitTermination() throws InterruptedException { terminated.await(); }
    /** Integrate into any HTTP server; raw request bytes must be preserved. */
    public WebhookHandler webhook() { return new WebhookHandler(config.appSecret(), this::accept); }

    private void accept(BotEvent event) {
        if (closed) throw new RejectedExecutionException("Bot is closed");
        events.execute(() -> {
            String key = dedupKey(event); long now = System.currentTimeMillis();
            synchronized (seen) {
                seen.entrySet().removeIf(e -> now - e.getValue() > 300_000);
                if (key != null && seen.containsKey(key)) return;
            }
            boolean succeeded = true;
            for (Consumer<BotEvent> listener : listeners) {
                try { listener.accept(event); }
                catch (RuntimeException error) { succeeded = false; report(error); }
            }
            if (succeeded && key != null) synchronized (seen) {
                if (seen.size() >= 10_000) seen.remove(seen.keySet().iterator().next());
                seen.put(key, now);
            }
        });
    }
    private static String dedupKey(BotEvent event) {
        if (event.eventId() != null && !event.eventId().isBlank()) return event.type() + ":" + event.eventId();
        JSONObject d = JSON.parseObject(event.dataJson());
        String id = d.getString("id");
        if (id == null) return null;
        return event.type() + ":" + id + ":" + d.get("msg_seq") + ":" + d.get("message_scene");
    }
    private void report(Throwable error) {
        if (errors.isEmpty()) logger.warn("QQ SDK callback failed: {}", error.getClass().getSimpleName());
        for (Consumer<Throwable> listener : errors) {
            try { listener.accept(error); } catch (RuntimeException ignored) { logger.warn("QQ SDK error listener failed"); }
        }
    }
    @Override public synchronized void close() {
        if (closed) return;
        closed = true; gateway.close();
        if (ownsEvents) events.shutdown();
        terminated.countDown();
    }
    public static final class Builder {
        private final QQBotConfig config;
        private HttpClient http;
        private AccessTokenProvider tokens;
        private ExecutorService executor;
        private int queueCapacity = 1024;
        private Builder(QQBotConfig config) { this.config = config; }
        public Builder httpClient(HttpClient v) { http = Objects.requireNonNull(v); return this; }
        public Builder tokenProvider(AccessTokenProvider v) { tokens = Objects.requireNonNull(v); return this; }
        /** Injected executors remain caller-owned; use a single worker for ordered dispatch. */
        public Builder eventExecutor(ExecutorService v) { executor = Objects.requireNonNull(v); return this; }
        public Builder eventQueueCapacity(int v) { if (v < 1) throw new IllegalArgumentException("Queue capacity must be positive"); queueCapacity = v; return this; }
        public QQBot build() { return new QQBot(this); }
    }
}
