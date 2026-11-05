package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Drives a real local HTTP server: protocol/encoding assertions do not mock transport. */
class ApiClientTest {
    private record Received(String method, String path, String query, String authorization, String contentType, String body) {}
    private record Reply(int status, String body) {}
    private HttpServer server;
    private final Queue<Reply> replies = new ConcurrentLinkedQueue<>();
    private final BlockingQueue<Received> received = new LinkedBlockingQueue<>();
    private AccessTokenProvider tokens;
    private ApiClient api;
    private QQBotConfig config;
    private final AtomicLong now = new AtomicLong(1_000_000);

    @BeforeEach void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            received.add(new Received(exchange.getRequestMethod(), exchange.getRequestURI().getRawPath(), exchange.getRequestURI().getRawQuery(),
                    exchange.getRequestHeaders().getFirst("Authorization"), exchange.getRequestHeaders().getFirst("Content-Type"),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            Reply reply = replies.poll(); if (reply == null) reply = new Reply(200, "{\"id\":\"sent\"}");
            byte[] body = reply.body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("X-Tps-trace-ID", "trace-test");
            exchange.getResponseHeaders().set("Retry-After", "2");
            exchange.sendResponseHeaders(reply.status, reply.status == 204 ? -1 : body.length);
            if (reply.status != 204) exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
        config = QQBotConfig.builder().appId("app").appSecret("secret").apiBase(base).tokenEndpoint(base.resolve("/app/getAppAccessToken")).build();
        tokens = mock(AccessTokenProvider.class); when(tokens.getToken()).thenReturn("token");
        api = new ApiClient(config, tokens, HttpClient.newHttpClient(), now::get);
    }
    @AfterEach void cleanup() { server.stop(0); }
    private Received take() throws InterruptedException {
        Received request = received.poll(5, TimeUnit.SECONDS); assertNotNull(request); return request;
    }

    @ParameterizedTest @EnumSource(ApiEndpoint.class)
    void everyPublishedOperationHasCorrectVerbPathQueryAndPayload(ApiEndpoint endpoint) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        var matcher = Pattern.compile("\\{([^}]+)}").matcher(endpoint.path());
        String expected = endpoint.path();
        while (matcher.find()) {
            String name = matcher.group(1); params.put(name, "value /中文");
            expected = expected.replace("{" + name + "}", "value%20%2F%E4%B8%AD%E6%96%87");
        }
        ApiRequest request = endpoint.method().equals("GET") ? ApiRequest.empty() : ApiRequest.json(Map.of("custom_field", Map.of("enabled", true)));
        String[] words = endpoint.name().toLowerCase(Locale.ROOT).split("_"); String name = words[0];
        for (int i = 1; i < words.length; i++) name += Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
        List<Object> arguments = new ArrayList<>(params.values()); arguments.add(request.query(Map.of("cursor", "a+b &中文")));
        Class<?>[] types = new Class<?>[arguments.size()]; Arrays.fill(types, String.class); types[types.length - 1] = ApiRequest.class;
        ApiResponse response = (ApiResponse) QQOpenApi.class.getMethod(name, types).invoke(new QQOpenApi(api), arguments.toArray());
        Received sent = take();
        assertEquals(endpoint.method(), sent.method); assertEquals(expected, sent.path);
        assertEquals("cursor=a%2Bb%20%26%E4%B8%AD%E6%96%87", sent.query);
        assertEquals("trace-test", response.traceId());
        if (endpoint.path().equals("/app/getAppAccessToken")) {
            assertNull(sent.authorization); verifyNoInteractions(tokens);
        } else assertEquals("QQBot token", sent.authorization);
        if (!endpoint.method().equals("GET")) assertEquals(Boolean.TRUE, JSON.parseObject(sent.body).getJSONObject("custom_field").get("enabled"));
    }

    @Test void authenticationRetryKeepsExactlyTheSameMessageAndSequence() throws Exception {
        replies.add(new Reply(401, "{\"code\":11243}"));
        when(tokens.refreshRejected("token")).thenReturn("fresh-token");
        api.send(MessageTarget.group("group"), Message.markdown("hello").keyboard(Map.of("id", "keyboard")), "incoming");
        Received first = take(), second = take();
        assertEquals(first.body, second.body); assertEquals(1, JSON.parseObject(first.body).getIntValue("msg_seq"));
        assertEquals("QQBot fresh-token", second.authorization); verify(tokens).refreshRejected("token");
    }
    @Test void realTokenRequestUsesConfiguredAddressCredentialsAndCachesTheResult() throws Exception {
        replies.add(new Reply(200, "{\"access_token\":\"local-token\",\"expires_in\":100}"));
        TokenManager provider = new TokenManager(config, HttpClient.newHttpClient(), now::get);
        assertEquals("local-token", provider.getToken()); assertEquals("local-token", provider.getToken());
        Received request = take(); assertEquals("POST", request.method); assertEquals("/app/getAppAccessToken", request.path);
        assertNull(request.authorization); assertEquals("app", JSON.parseObject(request.body).getString("appId"));
        assertEquals("secret", JSON.parseObject(request.body).getString("clientSecret")); assertTrue(received.isEmpty());
    }
    @Test void exhaustedReplyCounterCannotRestartAtOneAndTargetsAreIsolated() throws Exception {
        for (int i = 1; i <= 5; i++) {
            api.send(MessageTarget.group("one"), Message.text("hello"), "same");
            assertEquals(i, JSON.parseObject(take().body).getIntValue("msg_seq"));
        }
        assertEquals(QQApiException.Kind.REPLY_LIMIT, assertThrows(QQApiException.class,
                () -> api.send(MessageTarget.group("one"), Message.text("sixth"), "same")).kind());
        api.send(MessageTarget.group("two"), Message.text("hello"), "same"); assertEquals(1, JSON.parseObject(take().body).getIntValue("msg_seq"));
        now.addAndGet(300_001);
        api.send(MessageTarget.group("one"), Message.text("hello"), "new-incoming"); assertEquals(1, JSON.parseObject(take().body).getIntValue("msg_seq"));
    }
    @Test void concurrentRepliesAllocateUniqueSequences() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 5; i++) futures.add(pool.submit(() -> api.send(MessageTarget.group("g"), Message.text("hello"), "m")));
            for (Future<?> future : futures) future.get(5, TimeUnit.SECONDS);
            Set<Integer> sequence = new HashSet<>(); for (int i = 0; i < 5; i++) sequence.add(JSON.parseObject(take().body).getIntValue("msg_seq"));
            assertEquals(Set.of(1, 2, 3, 4, 5), sequence);
        } finally { pool.shutdownNow(); }
    }
    @Test void typedErrorsIncludeTraceAndRateLimitWithoutRetrying() throws Exception {
        replies.add(new Reply(429, "{\"err_code\":50002,\"message\":\"changed message\"}"));
        QQApiException e = assertThrows(QQApiException.class, () -> api.send(MessageTarget.group("g"), Message.text("hello"), null));
        assertEquals(QQApiException.Kind.RATE_LIMIT, e.kind()); assertEquals(50002L, e.platformCode());
        assertEquals("trace-test", e.traceId()); assertEquals("2", e.retryAfter()); take(); assertTrue(received.isEmpty());
        replies.add(new Reply(200, "{\"err_code\":40034005,\"message\":\"expired\"}"));
        assertEquals(40034005L, assertThrows(QQApiException.class, () -> api.send(MessageTarget.user("u"), Message.text("hello"), "m")).platformCode());
    }
    @Test void transportFailureReturnsUnknownOutcomeWithoutResending() throws Exception {
        HttpClient broken = mock(HttpClient.class);
        when(broken.send(any(HttpRequest.class), any())).thenThrow(new IOException("secret must not leak"));
        ApiClient client = new ApiClient(config, tokens, broken);
        QQApiException error = assertThrows(QQApiException.class, () -> client.send(MessageTarget.group("g"), Message.text("private"), null));
        assertTrue(error.mayHaveSucceeded()); assertFalse(error.toString().contains("secret"));
        verify(broken, times(1)).send(any(HttpRequest.class), any());
    }
    @Test void asyncReview204AndInvalidJsonAreDistinguished() {
        replies.add(new Reply(202, "{\"code\":304024}"));
        assertTrue(api.send(MessageTarget.group("g"), Message.text("hello"), null).pending());
        replies.add(new Reply(200, "{\"code\":304023}"));
        assertTrue(api.send(MessageTarget.group("g"), Message.text("hello"), null).pending());
        replies.add(new Reply(204, "")); assertEquals(204, api.answerInteraction("interaction", 0).statusCode());
        replies.add(new Reply(200, "not-json")); assertEquals(QQApiException.Kind.INVALID_RESPONSE,
                assertThrows(QQApiException.class, api::gatewayUrl).kind());
    }
    @Test void explicitJsonNullsArePreservedForPatchAndMessages() throws Exception {
        Map<String, Object> fields = new LinkedHashMap<>(); fields.put("nullable", null);
        new QQOpenApi(api).patchChannelsChannelId("channel", ApiRequest.json(fields));
        var first = JSON.parseObject(take().body); assertTrue(first.containsKey("nullable")); assertNull(first.get("nullable"));
        api.send(MessageTarget.user("u"), Message.json(fields), null);
        assertTrue(JSON.parseObject(take().body).containsKey("nullable"));
    }
    @Test void multipartStreamsTheActualFileAndPreservesJsonFields(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("photo.png"); Files.writeString(file, "image-binary");
        new QQOpenApi(api).postChannelsChannelIdMessages("channel", ApiRequest.multipart(Map.of("markdown", Map.of("content", "中文")),
                List.of(new ApiRequest.FilePart("file_image", "photo.png", "image/png", file))));
        Received sent = take(); assertTrue(sent.contentType.startsWith("multipart/form-data; boundary="));
        assertTrue(sent.body.contains("filename=\"photo.png\"")); assertTrue(sent.body.contains("image-binary"));
        assertTrue(sent.body.contains("{\"content\":\"中文\"}"));
    }
    @Test void generatedNamedMethodsExistForEveryEndpoint() throws Exception {
        Set<String> names = new HashSet<>(); for (var method : QQOpenApi.class.getDeclaredMethods()) names.add(method.getName());
        for (ApiEndpoint ep : ApiEndpoint.values()) {
            String[] words = ep.name().toLowerCase(Locale.ROOT).split("_"); String name = words[0];
            for (int i = 1; i < words.length; i++) name += Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
            assertTrue(names.contains(name), ep.name());
        }
    }
}
