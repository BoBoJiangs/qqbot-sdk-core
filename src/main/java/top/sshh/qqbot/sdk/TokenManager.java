package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Per-client token cache. The synchronized refresh merges concurrent refreshes. */
public final class TokenManager implements AccessTokenProvider {
    private final QQBotConfig config;
    private final HttpClient http;
    private final LongSupplier clock;
    private String token;
    private long expiresAt, refreshAt, retryAt;
    private QQApiException lastFailure;

    public TokenManager(QQBotConfig config, HttpClient http) { this(config, http, System::currentTimeMillis); }
    public TokenManager(QQBotConfig config, HttpClient http, LongSupplier clock) {
        this.config = Objects.requireNonNull(config); this.http = Objects.requireNonNull(http); this.clock = Objects.requireNonNull(clock);
    }
    @Override public synchronized String getToken() {
        long now = clock.getAsLong();
        if (token != null && now < refreshAt) return token;
        if (now < retryAt && lastFailure != null) {
            if (token != null && now < expiresAt) return token;
            throw lastFailure;
        }
        try { refresh(); }
        catch (QQApiException failure) {
            lastFailure = failure; retryAt = clock.getAsLong() + 1000;
            if (token == null || clock.getAsLong() >= expiresAt) throw failure;
        }
        if (token == null || clock.getAsLong() >= expiresAt) throw new QQApiException(QQApiException.Kind.AUTHENTICATION);
        return token;
    }
    @Override public synchronized void invalidateToken() {
        token = null; expiresAt = refreshAt = retryAt = 0; lastFailure = null;
    }
    @Override public synchronized String refreshRejected(String rejectedToken) {
        // A late 401 from the old generation must not invalidate a newer token.
        if (token != null && !token.equals(rejectedToken) && clock.getAsLong() < expiresAt) return token;
        if (token == null && lastFailure != null && clock.getAsLong() < retryAt) throw lastFailure;
        invalidateToken();
        return getToken();
    }
    public synchronized void forceRefresh() { invalidateToken(); getToken(); }

    private void refresh() {
        if (config.appId().isBlank() || config.appSecret().isBlank()) throw new QQApiException(QQApiException.Kind.AUTHENTICATION);
        HttpRequest request = HttpRequest.newBuilder(config.tokenEndpoint()).timeout(config.timeout())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSONObject.of("appId", config.appId(), "clientSecret", config.appSecret()).toJSONString())).build();
        long requestedAt = clock.getAsLong();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject data;
            try { data = JSON.parseObject(response.body()); }
            catch (RuntimeException e) { throw new QQApiException(QQApiException.Kind.INVALID_RESPONSE); }
            Long code = data == null ? null : data.getLong("code");
            if (response.statusCode() != 200 || data == null || (code != null && code != 0))
                throw new QQApiException(response.statusCode() == 429 || Objects.equals(code, 100001L)
                        ? QQApiException.Kind.RATE_LIMIT : QQApiException.Kind.AUTHENTICATION,
                        response.statusCode(), code, ApiClient.traceId(response, data), null);
            String value = data.getString("access_token");
            Long ttl = data.getLong("expires_in");
            if (value == null || value.isBlank() || ttl == null || ttl <= 0) throw new QQApiException(QQApiException.Kind.INVALID_RESPONSE);
            long lifetime = Math.multiplyExact(ttl, 1000L);
            long expiry = Math.addExact(requestedAt, lifetime);
            if (expiry <= clock.getAsLong()) throw new QQApiException(QQApiException.Kind.AUTHENTICATION);
            token = value; expiresAt = expiry;
            refreshAt = expiry - Math.min(config.refreshAhead().toMillis(), Math.max(1, lifetime / 10));
            retryAt = 0; lastFailure = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); throw new QQApiException(QQApiException.Kind.TRANSPORT);
        } catch (IOException e) { throw new QQApiException(QQApiException.Kind.TRANSPORT); }
        catch (ArithmeticException | NumberFormatException e) { throw new QQApiException(QQApiException.Kind.INVALID_RESPONSE); }
    }
}
