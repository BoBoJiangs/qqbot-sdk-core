import top.sshh.qqbot.sdk.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Local HTTP adapter; expose through an HTTPS reverse proxy configured in QQ. */
public final class WebhookServer {
    public static void main(String[] args) throws Exception {
        QQBotConfig config = QQBotConfig.builder().appId(System.getenv("OFFICIAL_APP_ID"))
                .appSecret(System.getenv("OFFICIAL_CLIENT_SECRET")).build();
        QQBot bot = QQBot.builder(config).build();
        bot.onGroupMessage(message -> bot.reply(message, Message.text("收到：" + message.content())));
        WebhookHandler handler = bot.webhook();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/qqbot", exchange -> {
            var result = handler.handle(exchange.getRequestHeaders().getFirst("X-Signature-Timestamp"),
                    exchange.getRequestHeaders().getFirst("X-Signature-Ed25519"),
                    exchange.getRequestBody().readNBytes(8 * 1024 * 1024 + 1));
            byte[] bytes = result.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(result.status(), bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { server.stop(0); bot.close(); }));
        server.start();
    }
}
