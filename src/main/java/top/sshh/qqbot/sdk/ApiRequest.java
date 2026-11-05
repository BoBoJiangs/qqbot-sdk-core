package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import java.io.FileNotFoundException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/** Immutable request. JSON maps preserve every official field, including new fields. */
public final class ApiRequest {
    public record FilePart(String name, String filename, String contentType, Path path) {
        public FilePart {
            safe(name); safe(filename); safe(contentType); Objects.requireNonNull(path);
        }
    }
    private final Map<String, ?> query;
    private final Map<String, String> headers;
    private final HttpRequest.BodyPublisher body;
    private final String contentType;
    private ApiRequest(Map<String, ?> query, Map<String, String> headers, HttpRequest.BodyPublisher body, String contentType) {
        this.query = Map.copyOf(query); this.headers = Map.copyOf(headers); this.body = body; this.contentType = contentType;
    }
    public static ApiRequest empty() { return new ApiRequest(Map.of(), Map.of(), HttpRequest.BodyPublishers.noBody(), null); }
    public static ApiRequest json(Map<String, ?> body) {
        return bytes(JSON.toJSONBytes(Objects.requireNonNull(body), JSONWriter.Feature.WriteNulls), "application/json; charset=utf-8");
    }
    public static ApiRequest bytes(byte[] body, String contentType) {
        safe(contentType);
        return new ApiRequest(Map.of(), Map.of(), HttpRequest.BodyPublishers.ofByteArray(body.clone()), contentType);
    }
    /** Files are streamed rather than loaded into the Java heap. */
    public static ApiRequest file(Path file, String contentType) {
        safe(contentType);
        try { return new ApiRequest(Map.of(), Map.of(), HttpRequest.BodyPublishers.ofFile(file), contentType); }
        catch (FileNotFoundException e) { throw new IllegalArgumentException("Upload file does not exist", e); }
    }
    public ApiRequest query(Map<String, ?> values) { return new ApiRequest(values, headers, body, contentType); }
    public ApiRequest header(String name, String value) {
        safe(name); safe(value);
        if (Set.of("authorization", "host", "content-length", "content-type").contains(name.toLowerCase(Locale.ROOT)))
            throw new IllegalArgumentException("Header is managed by the SDK");
        Map<String, String> copy = new LinkedHashMap<>(headers); copy.put(name, value);
        return new ApiRequest(query, copy, body, contentType);
    }
    public static ApiRequest multipart(Map<String, ?> fields, List<FilePart> files) {
        String boundary = "qqbot-" + UUID.randomUUID();
        List<HttpRequest.BodyPublisher> parts = new ArrayList<>();
        fields.forEach((name, value) -> {
            safe(name);
            String text = value instanceof String s ? s : JSON.toJSONString(value, JSONWriter.Feature.WriteNulls);
            parts.add(text("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + text + "\r\n"));
        });
        for (FilePart f : files) {
            parts.add(text("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + f.name()
                    + "\"; filename=\"" + f.filename() + "\"\r\nContent-Type: " + f.contentType() + "\r\n\r\n"));
            try { parts.add(HttpRequest.BodyPublishers.ofFile(f.path())); }
            catch (FileNotFoundException e) { throw new IllegalArgumentException("Upload file does not exist", e); }
            parts.add(text("\r\n"));
        }
        parts.add(text("--" + boundary + "--\r\n"));
        return new ApiRequest(Map.of(), Map.of(), HttpRequest.BodyPublishers.concat(parts.toArray(HttpRequest.BodyPublisher[]::new)),
                "multipart/form-data; boundary=" + boundary);
    }
    private static HttpRequest.BodyPublisher text(String value) { return HttpRequest.BodyPublishers.ofString(value, StandardCharsets.UTF_8); }
    private static void safe(String value) {
        if (value == null || value.isBlank() || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('"') >= 0)
            throw new IllegalArgumentException("Invalid multipart/header value");
    }
    Map<String, ?> query() { return query; }
    Map<String, String> headers() { return headers; }
    HttpRequest.BodyPublisher body() { return body; }
    String contentType() { return contentType; }
}
