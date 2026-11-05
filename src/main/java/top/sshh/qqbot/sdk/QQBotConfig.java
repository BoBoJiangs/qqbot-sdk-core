package top.sshh.qqbot.sdk;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/** Immutable SDK configuration. Credentials never appear in toString(). */
public final class QQBotConfig {
    private final String appId;
    private final String appSecret;
    private final URI apiBase;
    private final URI tokenEndpoint;
    private final Duration timeout;
    private final Duration refreshAhead;
    private final Duration replyWindow;
    private final int maxReplies;
    private final int maxReplyEntries;
    private final int intents;
    private final int shardId, shardCount;

    private QQBotConfig(Builder b) {
        appId = Objects.requireNonNull(b.appId); appSecret = Objects.requireNonNull(b.appSecret);
        apiBase = absolute(b.apiBase); tokenEndpoint = absolute(b.tokenEndpoint);
        timeout = positive(b.timeout); refreshAhead = positive(b.refreshAhead); replyWindow = positive(b.replyWindow);
        if (b.maxReplies < 1 || b.maxReplyEntries < 1) throw new IllegalArgumentException("Reply limits must be positive");
        maxReplies = b.maxReplies; maxReplyEntries = b.maxReplyEntries; intents = b.intents;
        if (b.shardCount < 1 || b.shardId < 0 || b.shardId >= b.shardCount) throw new IllegalArgumentException("Invalid shard ID/count");
        shardId = b.shardId; shardCount = b.shardCount;
    }
    private static URI absolute(URI uri) {
        if (uri == null || !uri.isAbsolute() || !("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null)
            throw new IllegalArgumentException("Expected an absolute HTTP(S) endpoint without credentials, query or fragment");
        return uri;
    }
    private static Duration positive(Duration d) {
        if (d == null || d.isNegative() || d.isZero() || d.toMillis() < 1) throw new IllegalArgumentException("Duration must be positive");
        return d;
    }
    public static Builder builder() { return new Builder(); }
    public String appId() { return appId; }
    public String appSecret() { return appSecret; }
    public URI apiBase() { return apiBase; }
    public URI tokenEndpoint() { return tokenEndpoint; }
    public Duration timeout() { return timeout; }
    public Duration refreshAhead() { return refreshAhead; }
    public Duration replyWindow() { return replyWindow; }
    public int maxReplies() { return maxReplies; }
    public int maxReplyEntries() { return maxReplyEntries; }
    public int intents() { return intents; }
    public int shardId() { return shardId; }
    public int shardCount() { return shardCount; }
    @Override public String toString() { return "QQBotConfig[appId=" + appId + ", credentials=REDACTED]"; }

    public static final class Builder {
        private String appId = "", appSecret = "";
        private URI apiBase = URI.create("https://api.bot.qq.com");
        private URI tokenEndpoint = URI.create("https://api.bot.qq.com/app/getAppAccessToken");
        private Duration timeout = Duration.ofSeconds(10), refreshAhead = Duration.ofSeconds(60), replyWindow = Duration.ofMinutes(5);
        private int maxReplies = 5, maxReplyEntries = 10_000, intents = (1 << 25) | (1 << 26);
        private int shardId, shardCount = 1;
        public Builder appId(String v) { appId = v; return this; }
        public Builder appSecret(String v) { appSecret = v; return this; }
        public Builder apiBase(URI v) { apiBase = v; return this; }
        public Builder tokenEndpoint(URI v) { tokenEndpoint = v; return this; }
        public Builder timeout(Duration v) { timeout = v; return this; }
        public Builder refreshAhead(Duration v) { refreshAhead = v; return this; }
        public Builder replyWindow(Duration v) { replyWindow = v; return this; }
        public Builder maxReplies(int v) { maxReplies = v; return this; }
        public Builder maxReplyEntries(int v) { maxReplyEntries = v; return this; }
        public Builder intents(int v) { intents = v; return this; }
        public Builder shard(int id, int count) { shardId = id; shardCount = count; return this; }
        public QQBotConfig build() { return new QQBotConfig(this); }
    }
}
