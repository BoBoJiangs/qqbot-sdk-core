package top.sshh.qqbot.sdk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaUploaderTest {
    @Test void signedUrlUploadStreamsFileAndUsesOnlyExplicitUploadHeaders(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("part.bin"); Files.writeString(file, "part-content");
        HttpClient http = mock(HttpClient.class); when(http.followRedirects()).thenReturn(HttpClient.Redirect.NEVER);
        HttpResponse<String> response = mock(HttpResponse.class); when(response.statusCode()).thenReturn(200); when(response.body()).thenReturn("");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        var uploader = new MediaUploader(http, Duration.ofSeconds(10));
        uploader.upload(URI.create("https://upload.example.test/part?signature=signed"), "PUT", Map.of("Content-MD5", "checksum"), file);
        var capture = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).send(capture.capture(), any(HttpResponse.BodyHandler.class));
        assertEquals("PUT", capture.getValue().method()); assertTrue(capture.getValue().headers().firstValue("Authorization").isEmpty());
        assertEquals("checksum", capture.getValue().headers().firstValue("Content-MD5").orElseThrow());
        assertEquals(Files.size(file), capture.getValue().bodyPublisher().orElseThrow().contentLength());
        assertThrows(IllegalArgumentException.class, () -> uploader.upload(URI.create("https://upload.example.test/part"), "PUT",
                Map.of("Authorization", "QQBot private-token"), file));
    }
    @Test void redirectsAreDisabledForPresignedUploads() {
        HttpClient http = mock(HttpClient.class); when(http.followRedirects()).thenReturn(HttpClient.Redirect.ALWAYS);
        assertThrows(IllegalArgumentException.class, () -> new MediaUploader(http, Duration.ofSeconds(10)));
    }
}
