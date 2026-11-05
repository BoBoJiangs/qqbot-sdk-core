package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/** HTTP-framework-independent Webhook adapter. Return status/body unchanged to QQ. */
public final class WebhookHandler {
    public record Response(int status, String body) {}
    private final WebhookVerifier verifier;
    private final BotEventHandler events;
    private final Clock clock;
    private final long toleranceSeconds;
    public WebhookHandler(String secret, BotEventHandler events) {
        this(secret, events, Clock.systemUTC(), Duration.ofMinutes(5));
    }
    public WebhookHandler(String secret, BotEventHandler events, Clock clock, Duration tolerance) {
        verifier = new WebhookVerifier(secret); this.events = Objects.requireNonNull(events); this.clock = Objects.requireNonNull(clock);
        if (tolerance == null || tolerance.isNegative() || tolerance.isZero()) throw new IllegalArgumentException("Positive replay window required");
        toleranceSeconds = tolerance.toSeconds();
    }
    public Response handle(String timestamp, String signature, byte[] originalBody) {
        if (originalBody != null && originalBody.length > 8 * 1024 * 1024) return new Response(413, "");
        JSONObject payload; Integer op;
        try {
            payload = JSON.parseObject(new String(originalBody, StandardCharsets.UTF_8));
            op = payload == null ? null : payload.getInteger("op");
        }
        catch (RuntimeException e) { return new Response(400, ""); }
        if (payload == null || op == null) return new Response(400, "");
        if (op == 13) {
            JSONObject d;
            try { d = payload.getJSONObject("d"); }
            catch (RuntimeException e) { return new Response(400, ""); }
            if (d == null || d.getString("event_ts") == null || d.getString("plain_token") == null) return new Response(400, "");
            return new Response(200, JSON.toJSONString(verifier.validationResponse(d.getString("event_ts"), d.getString("plain_token"))));
        }
        // QQ's initial op13 challenge has no signature headers. Only a signed
        // dispatch may enter business code; the challenge only proves key ownership.
        try {
            long delta = Math.subtractExact(clock.instant().getEpochSecond(), Long.parseLong(timestamp));
            if (delta < -toleranceSeconds || delta > toleranceSeconds || !verifier.verify(timestamp, signature, originalBody))
                return new Response(401, "");
        } catch (RuntimeException e) { return new Response(401, ""); }
        if (op != 0 || payload.getString("t") == null || payload.getString("t").isBlank()) return new Response(400, "");
        BotEvent event;
        try { event = BotEvent.fromJson(payload.toJSONString(JSONWriter.Feature.WriteNulls)); }
        catch (RuntimeException e) { return new Response(400, ""); }
        try { events.onEvent(event); }
        catch (RuntimeException e) { return new Response(503, ""); }
        return new Response(200, "{\"op\":12,\"d\":0}");
    }
}
