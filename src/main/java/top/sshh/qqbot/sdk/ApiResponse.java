package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import java.util.Map;

/** Platform acknowledgement; accepted/review-pending responses do not prove delivery. */
public record ApiResponse(int statusCode, String traceId, String body) {
    public Map<String, Object> object() {
        return body == null || body.isBlank() ? Map.of() : JSON.parseObject(body);
    }
    public boolean pending() {
        Object code = object().getOrDefault("err_code", object().get("code"));
        return statusCode == 201 || statusCode == 202 || "304023".equals(String.valueOf(code)) || "304024".equals(String.valueOf(code));
    }
    public String messageId() { Object id = object().get("id"); return id == null ? null : id.toString(); }
}
