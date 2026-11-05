package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/** Common message fields; raw contains every scene-specific field and attachment. */
public record MessageEvent(String id, MessageTarget target, String authorId, String content, BotEvent raw) {
    public static MessageEvent from(BotEvent event) {
        JSONObject data = JSON.parseObject(event.dataJson());
        MessageTarget target = switch (event.type()) {
            case "GROUP_MESSAGE_CREATE", "GROUP_AT_MESSAGE_CREATE" -> MessageTarget.group(data.getString("group_openid"));
            case "C2C_MESSAGE_CREATE" -> MessageTarget.user(data.getJSONObject("author").getString("user_openid"));
            case "AT_MESSAGE_CREATE", "MESSAGE_CREATE" -> MessageTarget.channel(data.getString("channel_id"));
            case "DIRECT_MESSAGE_CREATE" -> MessageTarget.direct(data.getString("guild_id"));
            default -> throw new IllegalArgumentException("Not a message-create event");
        };
        JSONObject author = data.getJSONObject("author");
        String user = author == null ? null : author.getString("member_openid");
        if (user == null && author != null) user = author.getString("user_openid");
        if (user == null && author != null) user = author.getString("id");
        String content = data.getString("content");
        return new MessageEvent(data.getString("id"), target, user, content == null ? "" : content, event);
    }
}
