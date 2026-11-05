package top.sshh.qqbot.sdk;

import java.util.LinkedHashMap;
import java.util.Map;

/** Stateful single-chat streaming session. One session serializes its own updates. */
public final class C2CStream {
    private final QQOpenApi api;
    private final String userId, replyTo;
    private String streamId;
    private int index;
    private boolean finished;
    public C2CStream(QQOpenApi api, String userId, String replyTo) {
        this.api = java.util.Objects.requireNonNull(api);
        if (userId == null || userId.isBlank() || replyTo == null || replyTo.isBlank()) throw new IllegalArgumentException("User and reply message ID required");
        this.userId = userId; this.replyTo = replyTo;
    }
    public synchronized ApiResponse append(String markdown) { return update(markdown, "append", false); }
    public synchronized ApiResponse replace(String markdown) { return update(markdown, "replace", false); }
    public synchronized ApiResponse complete(String markdown) { return update(markdown, "append", true); }
    private ApiResponse update(String content, String mode, boolean complete) {
        if (finished) throw new IllegalStateException("Stream is closed or its last delivery is uncertain");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("msg_id", replyTo); body.put("index", index); body.put("input_mode", mode);
        body.put("input_state", complete ? 10 : 1); body.put("content_type", "markdown"); body.put("content_raw", content);
        if (streamId != null) body.put("stream_msg_id", streamId);
        try {
            ApiResponse response = api.postV2UsersUserOpenidStreamMessages(userId, ApiRequest.json(body));
            String id = response.messageId();
            if (id == null || id.isBlank()) { finished = true; throw new QQApiException(QQApiException.Kind.INVALID_RESPONSE); }
            streamId = id; index++; finished = complete;
            return response;
        } catch (QQApiException e) { if (e.mayHaveSucceeded()) finished = true; throw e; }
    }
}
