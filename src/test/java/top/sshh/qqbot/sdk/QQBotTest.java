package top.sshh.qqbot.sdk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QQBotTest {
    private static final String SECRET = "test-secret";
    private QQBotConfig config() { return QQBotConfig.builder().appId("test-app").appSecret(SECRET).build(); }
    private WebhookHandler.Response deliver(QQBot bot, String type, String data) {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String body = "{\"op\":0,\"t\":\"" + type + "\",\"d\":" + data + "}";
        String signature = new WebhookVerifier(SECRET).validationResponse(timestamp, body).get("signature");
        return bot.webhook().handle(timestamp, signature, body.getBytes(StandardCharsets.UTF_8));
    }
    private ExecutorService directExecutor() {
        ExecutorService executor = mock(ExecutorService.class);
        doAnswer(call -> { call.<Runnable>getArgument(0).run(); return null; }).when(executor).execute(any());
        return executor;
    }
    @ParameterizedTest @EnumSource(EventType.class)
    void everyPublishedEventCanBeReceivedWithoutFrameworkDependencies(EventType type) {
        HttpClient http = mock(HttpClient.class); ExecutorService executor = directExecutor();
        List<BotEvent> observed = new ArrayList<>();
        try (QQBot bot = QQBot.builder(config()).httpClient(http).eventExecutor(executor).build()) {
            bot.on(type, observed::add); assertEquals(200, deliver(bot, type.name(), "{\"all_fields\":true}").status());
            assertEquals(type.name(), observed.get(0).type()); assertEquals(true, observed.get(0).data().get("all_fields"));
            verifyNoInteractions(http);
        }
        verify(executor, never()).shutdown();
    }
    @Test void typedGroupMessagesDeduplicateAndPreserveUnknownFields() {
        List<MessageEvent> messages = new ArrayList<>();
        try (QQBot bot = QQBot.builder(config()).eventExecutor(directExecutor()).build()) {
            bot.onGroupMessage(messages::add);
            String body = "{\"id\":\"message\",\"group_openid\":\"group\",\"author\":{\"member_openid\":\"member\"},\"content\":\"hello\",\"attachment\":{\"new_field\":1}}";
            deliver(bot, "GROUP_MESSAGE_CREATE", body); deliver(bot, "GROUP_MESSAGE_CREATE", body);
            assertEquals(1, messages.size()); assertEquals("member", messages.get(0).authorId());
            assertEquals(MessageTarget.group("group"), messages.get(0).target()); assertTrue(messages.get(0).raw().data().containsKey("attachment"));
        }
    }
    @Test void callbackFailuresAreReportedAndDoNotMarkFailedEventsAsProcessed() {
        AtomicInteger attempts = new AtomicInteger(); List<Throwable> failures = new ArrayList<>();
        try (QQBot bot = QQBot.builder(config()).eventExecutor(directExecutor()).build()) {
            bot.onError(failures::add).onEvent(event -> { if (attempts.incrementAndGet() == 1) throw new IllegalStateException(); });
            deliver(bot, "FUTURE_EVENT", "{\"id\":\"same\"}"); deliver(bot, "FUTURE_EVENT", "{\"id\":\"same\"}");
            assertEquals(2, attempts.get()); assertEquals(1, failures.size());
        }
    }
    @Test void fullEventQueueReturns503InsteadOfDroppingTheEvent() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1), completed = new CountDownLatch(2);
        try (QQBot bot = QQBot.builder(config()).eventQueueCapacity(1).build()) {
            bot.onEvent(event -> {
                entered.countDown();
                try { assertTrue(release.await(5, TimeUnit.SECONDS)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                completed.countDown();
            });
            assertEquals(200, deliver(bot, "FUTURE_EVENT", "{\"id\":\"one\"}").status()); assertTrue(entered.await(5, TimeUnit.SECONDS));
            assertEquals(200, deliver(bot, "FUTURE_EVENT", "{\"id\":\"two\"}").status());
            assertEquals(503, deliver(bot, "FUTURE_EVENT", "{\"id\":\"three\"}").status());
            release.countDown(); assertTrue(completed.await(5, TimeUnit.SECONDS));
        } finally { release.countDown(); }
    }
    @Test void closedBotsRejectIncomingEventsAndCannotRestart() {
        QQBot bot = QQBot.builder(config()).build(); bot.close(); bot.close();
        assertThrows(IllegalStateException.class, bot::start);
        assertEquals(503, deliver(bot, "FUTURE_EVENT", "{}").status());
    }
}
