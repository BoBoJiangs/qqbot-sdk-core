package top.sshh.qqbot.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/** Streams a file/part to a platform-provided presigned URL, without QQ credentials. */
public final class MediaUploader {
    private final HttpClient http;
    private final Duration timeout;
    public MediaUploader(HttpClient http, Duration timeout) {
        if (http.followRedirects() != HttpClient.Redirect.NEVER) throw new IllegalArgumentException("Presigned uploads require redirects disabled");
        this.http = http; this.timeout = timeout;
    }
    public ApiResponse upload(URI presignedUrl, String method, Map<String, String> headers, Path file) {
        if (!"https".equals(presignedUrl.getScheme()) || presignedUrl.getHost() == null || presignedUrl.getUserInfo() != null
                || !Set.of("PUT", "POST").contains(method)) throw new IllegalArgumentException("Expected a presigned HTTPS upload URL");
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(presignedUrl).timeout(timeout)
                    .method(method, HttpRequest.BodyPublishers.ofFile(file));
            headers.forEach((k, v) -> {
                if (k.equalsIgnoreCase("Authorization") && v.startsWith("QQBot "))
                    throw new IllegalArgumentException("QQ Authorization must not be sent to an upload host");
                request.header(k, v);
            });
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new QQApiException(QQApiException.Kind.REMOTE, response.statusCode(), null, null, null);
            return new ApiResponse(response.statusCode(), null, response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); throw new QQApiException(QQApiException.Kind.UNKNOWN_RESULT);
        } catch (IOException e) { throw new QQApiException(QQApiException.Kind.UNKNOWN_RESULT); }
    }
}
