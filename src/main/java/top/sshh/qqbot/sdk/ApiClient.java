package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared transport for every published endpoint. Mutating requests are never blindly retried. */
public final class ApiClient {
    private static final Pattern PARAMETER = Pattern.compile("\\{([^}]+)}");
    private final QQBotConfig config;
    private final AccessTokenProvider tokens;
    private final HttpClient http;
    private final LongSupplier clock;
    private final Map<String, Sequence> sequences = new LinkedHashMap<>();
    private record Sequence(long expiresAt, int next) {}

    public ApiClient(QQBotConfig config, AccessTokenProvider tokens, HttpClient http) {
        this(config, tokens, http, System::currentTimeMillis);
    }
    public ApiClient(QQBotConfig config, AccessTokenProvider tokens, HttpClient http, LongSupplier clock) {
        this.config = Objects.requireNonNull(config); this.tokens = Objects.requireNonNull(tokens);
        this.http = Objects.requireNonNull(http); this.clock = Objects.requireNonNull(clock);
    }
    public ApiResponse execute(ApiEndpoint endpoint, Map<String, String> parameters, ApiRequest request) {
        Objects.requireNonNull(endpoint); Objects.requireNonNull(parameters);
        Matcher matcher = PARAMETER.matcher(endpoint.path());
        StringBuffer path = new StringBuffer(); Set<String> used = new HashSet<>();
        while (matcher.find()) {
            String name = matcher.group(1), value = parameters.get(name);
            if (value == null || value.isBlank() || value.equals(".") || value.equals(".."))
                throw new IllegalArgumentException("Missing/invalid path parameter: " + name);
            used.add(name); matcher.appendReplacement(path, Matcher.quoteReplacement(encode(value)));
        }
        matcher.appendTail(path);
        if (!parameters.keySet().equals(used)) throw new IllegalArgumentException("Unexpected path parameter");
        return request(endpoint.method(), path.toString(), request);
    }
    /** Escape hatch for future platform additions; path stays on the configured API host. */
    public ApiResponse request(String method, String path, ApiRequest request) {
        if (!Set.of("GET", "POST", "PUT", "PATCH", "DELETE").contains(method)
                || !path.startsWith("/") || path.startsWith("//") || path.contains("?") || path.contains("#")
                || path.contains("{") || path.contains("\\") || Arrays.asList(path.split("/")).contains(".."))
            throw new IllegalArgumentException("Invalid API method/path");
        request = Objects.requireNonNull(request);
        boolean tokenRequest = path.equals("/app/getAppAccessToken");
        String base = config.apiBase().toString().replaceAll("/+$", "");
        String endpoint = tokenRequest ? config.tokenEndpoint().toString() : base + path;
        String query = queryString(request.query());
        URI uri = URI.create(endpoint + (query.isEmpty() ? "" : "?" + query));
        String token = tokenRequest ? null : tokens.getToken();
        for (int attempt = 0; attempt < 2; attempt++) {
            if (!tokenRequest && (token == null || token.isBlank())) throw new QQApiException(QQApiException.Kind.AUTHENTICATION);
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(config.timeout()).method(method, request.body());
            if (!tokenRequest) builder.header("Authorization", "QQBot " + token);
            builder.header("User-Agent", "qqbot-java/0.1.0");
            if (request.contentType() != null) builder.header("Content-Type", request.contentType());
            request.headers().forEach(builder::header);
            try {
                HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                if (!tokenRequest && response.statusCode() == 401 && attempt == 0) {
                    token = tokens.refreshRejected(token); continue;
                }
                return checked(response, method);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw transportFailure(method);
            } catch (IOException e) { throw transportFailure(method); }
        }
        throw new QQApiException(QQApiException.Kind.AUTHENTICATION);
    }
    private static QQApiException transportFailure(String method) {
        return new QQApiException(method.equals("GET") ? QQApiException.Kind.TRANSPORT : QQApiException.Kind.UNKNOWN_RESULT);
    }
    private static ApiResponse checked(HttpResponse<String> response, String method) {
        int status = response.statusCode(); String body = response.body(); JSONObject object = null;
        if (body != null && !body.isBlank()) {
            try { Object parsed = JSON.parse(body); if (parsed instanceof JSONObject obj) object = obj; }
            catch (RuntimeException e) {
                if (status >= 200 && status < 300) throw new QQApiException(method.equals("GET")
                        ? QQApiException.Kind.INVALID_RESPONSE : QQApiException.Kind.UNKNOWN_RESULT);
            }
        }
        Long code = null;
        if (object != null) {
            try { code = object.containsKey("err_code") ? object.getLong("err_code") : object.getLong("code"); }
            catch (RuntimeException e) { throw new QQApiException(QQApiException.Kind.INVALID_RESPONSE); }
        }
        String trace = traceId(response, object);
        boolean pending = status >= 200 && status < 300 && (Objects.equals(code, 304023L) || Objects.equals(code, 304024L));
        if (status < 200 || status >= 300 || (code != null && code != 0 && !pending)) {
            QQApiException.Kind kind = status == 401 ? QQApiException.Kind.AUTHENTICATION
                    : status == 429 || Objects.equals(code, 50002L) || Objects.equals(code, 1100100L)
                    ? QQApiException.Kind.RATE_LIMIT : QQApiException.Kind.REMOTE;
            String retry = response.headers() == null ? null : response.headers().firstValue("Retry-After").orElse(null);
            throw new QQApiException(kind, status, code, trace, retry);
        }
        return new ApiResponse(status, trace, body == null ? "" : body);
    }
    static String traceId(HttpResponse<?> response, JSONObject object) {
        String header = response.headers() == null ? null : response.headers().firstValue("X-Tps-trace-ID").orElse(null);
        return header != null ? header : object == null ? null : object.getString("trace_id");
    }
    public String gatewayUrl() {
        Object value = execute(ApiEndpoint.GET_GATEWAY, Map.of(), ApiRequest.empty()).object().get("url");
        if (!(value instanceof String s) || s.isBlank()) throw new QQApiException(QQApiException.Kind.INVALID_RESPONSE);
        return s;
    }
    public ApiResponse answerInteraction(String id, int code) {
        return execute(ApiEndpoint.PUT_INTERACTIONS_INTERACTION_ID, Map.of("interaction_id", id), ApiRequest.json(Map.of("code", code)));
    }
    public ApiResponse send(MessageTarget target, Message message, String replyTo) {
        Map<String, Object> body = message.body();
        if (replyTo != null && !replyTo.isBlank()) {
            body.put("msg_id", replyTo);
            if (target.scope() == MessageTarget.Scope.GROUP || target.scope() == MessageTarget.Scope.C2C)
                body.put("msg_seq", nextSequence(target, replyTo));
        }
        ApiEndpoint endpoint; String key;
        switch (target.scope()) {
            case GROUP -> { endpoint = ApiEndpoint.POST_V2_GROUPS_GROUP_OPENID_MESSAGES; key = "group_openid"; }
            case C2C -> { endpoint = ApiEndpoint.POST_V2_USERS_USER_OPENID_MESSAGES; key = "user_openid"; }
            case CHANNEL -> { endpoint = ApiEndpoint.POST_CHANNELS_CHANNEL_ID_MESSAGES; key = "channel_id"; body.remove("msg_type"); }
            case DIRECT -> { endpoint = ApiEndpoint.POST_DMS_GUILD_ID_MESSAGES; key = "guild_id"; body.remove("msg_type"); }
            default -> throw new IllegalArgumentException("Unsupported message scope");
        }
        return execute(endpoint, Map.of(key, target.id()), ApiRequest.json(body));
    }
    private synchronized int nextSequence(MessageTarget target, String replyTo) {
        long now = clock.getAsLong();
        sequences.entrySet().removeIf(e -> e.getValue().expiresAt <= now);
        String key = target.scope() + ":" + target.id() + ":" + replyTo;
        Sequence entry = sequences.get(key);
        if (entry == null) {
            // Never evict a live counter: doing so would reuse a platform sequence.
            if (sequences.size() >= config.maxReplyEntries()) throw new QQApiException(QQApiException.Kind.REPLY_LIMIT);
            entry = new Sequence(now + config.replyWindow().toMillis(), 1);
        }
        if (entry.next > config.maxReplies()) throw new QQApiException(QQApiException.Kind.REPLY_LIMIT);
        sequences.put(key, new Sequence(entry.expiresAt, entry.next + 1));
        return entry.next;
    }
    static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20"); }
    private static String queryString(Map<String, ?> values) {
        List<String> query = new ArrayList<>();
        values.forEach((k, v) -> {
            Iterable<?> items = v instanceof Iterable<?> list ? list : List.of(v);
            for (Object item : items) query.add(encode(k) + "=" + encode(String.valueOf(item)));
        });
        return String.join("&", query);
    }
}
