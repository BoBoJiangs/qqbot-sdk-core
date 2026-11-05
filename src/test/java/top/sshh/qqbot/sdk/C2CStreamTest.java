package top.sshh.qqbot.sdk;

import org.junit.jupiter.api.Test;
import com.alibaba.fastjson2.JSON;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class C2CStreamTest {
    @Test void followsTheReturnedStreamIdAndMonotonicallyIncreasingIndices() throws Exception {
        ApiClient api = mock(ApiClient.class); List<ApiRequest> calls = new ArrayList<>();
        when(api.execute(eq(ApiEndpoint.POST_V2_USERS_USER_OPENID_STREAM_MESSAGES), any(), any())).thenAnswer(call -> {
            calls.add(call.getArgument(2)); return new ApiResponse(200, "trace", "{\"id\":\"stream-id\"}");
        });
        C2CStream stream = new C2CStream(new QQOpenApi(api), "user", "incoming");
        stream.append("first"); stream.replace("first and second"); stream.complete("done");
        for (int i = 0; i < calls.size(); i++) {
            var body = JSON.parseObject(read(calls.get(i)));
            assertEquals(i, body.getIntValue("index")); assertEquals(i == 2 ? 10 : 1, body.getIntValue("input_state"));
            if (i == 0) assertFalse(body.containsKey("stream_msg_id")); else assertEquals("stream-id", body.getString("stream_msg_id"));
            assertEquals(i == 1 ? "replace" : "append", body.getString("input_mode"));
        }
        assertThrows(IllegalStateException.class, () -> stream.append("after end"));
    }
    @Test void anUnknownDeliveryOutcomePreventsUnsafeContinuation() {
        ApiClient api = mock(ApiClient.class);
        when(api.execute(any(), any(), any())).thenThrow(new QQApiException(QQApiException.Kind.UNKNOWN_RESULT));
        C2CStream stream = new C2CStream(new QQOpenApi(api), "user", "incoming");
        assertThrows(QQApiException.class, () -> stream.append("hello"));
        assertThrows(IllegalStateException.class, () -> stream.append("retry"));
        verify(api, times(1)).execute(any(), any(), any());
    }
    static String read(ApiRequest request) throws Exception {
        CompletableFuture<String> text = new CompletableFuture<>(); var out = new java.io.ByteArrayOutputStream();
        request.body().subscribe(new Flow.Subscriber<ByteBuffer>() {
            public void onSubscribe(Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
            public void onNext(ByteBuffer buffer) { byte[] bytes = new byte[buffer.remaining()]; buffer.get(bytes); out.writeBytes(bytes); }
            public void onError(Throwable error) { text.completeExceptionally(error); }
            public void onComplete() { text.complete(out.toString(java.nio.charset.StandardCharsets.UTF_8)); }
        });
        return text.get(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}
