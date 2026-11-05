package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Message value object; arbitrary official message types are available via json(). */
public final class Message {
    private final String json;
    private Message(Map<String, ?> body) { json = JSON.toJSONString(body, JSONWriter.Feature.WriteNulls); }
    public static Message text(String text) { return new Message(Map.of("content", text, "msg_type", 0)); }
    public static Message markdown(String content) { return new Message(Map.of("msg_type", 2, "markdown", Map.of("content", content))); }
    public static Message json(Map<String, ?> body) { return new Message(body); }
    public Message keyboard(Map<String, ?> keyboard) {
        Map<String, Object> body = body(); body.put("keyboard", keyboard); return new Message(body);
    }
    public Message reference(String messageId) {
        Map<String, Object> body = body();
        body.put("message_reference", Map.of("message_id", messageId, "ignore_get_message_error", false));
        return new Message(body);
    }
    Map<String, Object> body() { return new LinkedHashMap<>(JSON.parseObject(json)); }
}
