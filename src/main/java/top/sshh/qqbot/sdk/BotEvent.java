package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import java.util.Map;
import java.util.Objects;

/** Lossless event envelope; supports all published events and unknown future events. */
public record BotEvent(String type, String eventId, Long sequence, String dataJson) {
    public BotEvent { Objects.requireNonNull(type); Objects.requireNonNull(dataJson); }
    public Map<String, Object> data() { return JSON.parseObject(dataJson); }
    public static BotEvent fromJson(String payload) {
        JSONObject body = JSON.parseObject(payload);
        if (body == null || !Integer.valueOf(0).equals(body.getInteger("op")) || body.getString("t") == null || body.getString("t").isBlank())
            throw new IllegalArgumentException("Expected a dispatch event");
        JSONObject data = body.getJSONObject("d");
        return new BotEvent(body.getString("t"), body.getString("id"), body.getLong("s"), data == null ? "{}" : data.toJSONString(JSONWriter.Feature.WriteNulls));
    }
}
