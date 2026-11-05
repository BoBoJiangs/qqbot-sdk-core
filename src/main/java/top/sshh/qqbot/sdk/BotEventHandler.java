package top.sshh.qqbot.sdk;

/** Acceptance callback: enqueue quickly; throw if the event cannot be accepted. */
@FunctionalInterface
public interface BotEventHandler {
    void onEvent(BotEvent event);
}
