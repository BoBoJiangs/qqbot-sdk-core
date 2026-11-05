import top.sshh.qqbot.sdk.*;

/** Run with the SDK and its Maven runtime dependencies on the classpath. */
public final class StandaloneBot {
    public static void main(String[] args) throws InterruptedException {
        QQBotConfig config = QQBotConfig.builder()
                .appId(System.getenv("OFFICIAL_APP_ID"))
                .appSecret(System.getenv("OFFICIAL_CLIENT_SECRET")).build();
        try (QQBot bot = QQBot.builder(config).build()) {
            bot.onError(error -> System.err.println(error.getClass().getSimpleName()));
            bot.onGroupMessage(message -> bot.reply(message, Message.text("收到：" + message.content())));
            bot.on(EventType.INTERACTION_CREATE, event -> bot.api().answerInteraction((String) event.data().get("id"), 0));
            Runtime.getRuntime().addShutdownHook(new Thread(bot::close));
            bot.start();
            bot.awaitTermination();
        }
    }
}
