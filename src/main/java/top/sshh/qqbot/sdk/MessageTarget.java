package top.sshh.qqbot.sdk;

import java.util.Objects;

public record MessageTarget(Scope scope, String id) {
    public enum Scope { GROUP, C2C, CHANNEL, DIRECT }
    public MessageTarget {
        Objects.requireNonNull(scope);
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Target ID is required");
    }
    public static MessageTarget group(String id) { return new MessageTarget(Scope.GROUP, id); }
    public static MessageTarget user(String id) { return new MessageTarget(Scope.C2C, id); }
    public static MessageTarget channel(String id) { return new MessageTarget(Scope.CHANNEL, id); }
    public static MessageTarget direct(String id) { return new MessageTarget(Scope.DIRECT, id); }
}
