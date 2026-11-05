package top.sshh.qqbot.sdk;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WebhookTest {
    private static final String SECRET = "naOC0ocQE3shWLAfffVLB1rhYPG7";
    private static final String TIMESTAMP = "1725442341";
    private static final String BODY = "{ \"op\": 0,\"d\": {}, \"t\": \"GATEWAY_EVENT_NAME\"}";
    // Independently computed with Node crypto and the public key in QQ's docs.
    // The signature printed in that page differs from its stated raw-body input.
    private static final String SIGNATURE = "2eb9983ebb8bb209e78fd095942f58e442656656e7975d01e64f9023a84b7c964290fdd40e5500c33867ccfe9563b7e0b6bac0e1d42c13e787b304fd51f71102";

    @Test void matchesThePublishedOfficialKeyAndIndependentNodeSignature() throws Exception {
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertTrue(verifier.verify(TIMESTAMP, SIGNATURE, BODY.getBytes(StandardCharsets.UTF_8)));
        assertEquals(SIGNATURE, verifier.validationResponse(TIMESTAMP, BODY).get("signature"));
        byte[] publicKey = java.util.HexFormat.of().parseHex("302a300506032b6570032100d7c362fe78aef81ff23287b493628b5db02a3c4fe30b215e4d19609b5d76673a");
        var key = java.security.KeyFactory.getInstance("Ed25519").generatePublic(new java.security.spec.X509EncodedKeySpec(publicKey));
        var independent = java.security.Signature.getInstance("Ed25519"); independent.initVerify(key);
        independent.update((TIMESTAMP + BODY).getBytes(StandardCharsets.UTF_8));
        assertTrue(independent.verify(java.util.HexFormat.of().parseHex(SIGNATURE)));
        assertFalse(verifier.verify(TIMESTAMP, SIGNATURE, BODY.replace(" ", "").getBytes(StandardCharsets.UTF_8)));
        assertFalse(verifier.verify(TIMESTAMP, "00", BODY.getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> new WebhookVerifier(""));
    }
    private WebhookHandler handler(List<BotEvent> events) {
        return new WebhookHandler(SECRET, events::add, Clock.fixed(Instant.ofEpochSecond(Long.parseLong(TIMESTAMP)), ZoneOffset.UTC), Duration.ofMinutes(5));
    }
    private WebhookHandler.Response handle(WebhookHandler handler, String body) {
        String signature = new WebhookVerifier(SECRET).validationResponse(TIMESTAMP, body).get("signature");
        return handler.handle(TIMESTAMP, signature, body.getBytes(StandardCharsets.UTF_8));
    }
    @Test void authenticatesUnknownEventsAndAcknowledgesOnlyAfterAcceptance() {
        List<BotEvent> events = new ArrayList<>(); var handler = handler(events);
        var response = handle(handler, "{\"op\":0,\"id\":\"evt\",\"s\":7,\"t\":\"FUTURE_EVENT\",\"d\":{\"unknown\":true,\"nullable\":null}}");
        assertEquals(200, response.status()); assertEquals("{\"op\":12,\"d\":0}", response.body());
        assertEquals("FUTURE_EVENT", events.get(0).type()); assertEquals(true, events.get(0).data().get("unknown"));
        assertEquals("evt", events.get(0).eventId()); assertEquals(7L, events.get(0).sequence());
        assertTrue(events.get(0).data().containsKey("nullable"));
        var unavailable = new WebhookHandler(SECRET, e -> { throw new IllegalStateException(); },
                Clock.fixed(Instant.ofEpochSecond(Long.parseLong(TIMESTAMP)), ZoneOffset.UTC), Duration.ofMinutes(5));
        assertEquals(503, handle(unavailable, BODY).status());
    }
    @Test void callbackChallengeProducesTheOfficialResponseShape() {
        List<BotEvent> events = new ArrayList<>();
        var response = handle(handler(events), "{\"op\":13,\"d\":{\"event_ts\":\"123\",\"plain_token\":\"challenge\"}}");
        assertEquals(200, response.status());
        var object = JSON.parseObject(response.body()); assertEquals("challenge", object.getString("plain_token"));
        assertEquals(new WebhookVerifier(SECRET).validationResponse("123", "challenge").get("signature"), object.getString("signature"));
        assertTrue(events.isEmpty());
    }
    @Test void initialUnsignedChallengeWorksButUnsignedBusinessEventsAreRejected() {
        List<BotEvent> events = new ArrayList<>(); var handler = handler(events);
        byte[] challenge = "{\"op\":13,\"d\":{\"event_ts\":\"123\",\"plain_token\":\"challenge\"}}".getBytes(StandardCharsets.UTF_8);
        assertEquals(200, handler.handle(null, null, challenge).status());
        assertEquals(401, handler.handle(null, null, BODY.getBytes(StandardCharsets.UTF_8)).status());
        assertTrue(events.isEmpty());
    }
    @Test void invalidSignaturesStaleTimestampsAndMalformedEventsNeverReachBusinessCode() {
        List<BotEvent> events = new ArrayList<>(); var handler = handler(events);
        assertEquals(401, handler.handle(TIMESTAMP, "invalid", BODY.getBytes(StandardCharsets.UTF_8)).status());
        assertEquals(401, handler.handle("0", SIGNATURE, BODY.getBytes(StandardCharsets.UTF_8)).status());
        assertEquals(400, handle(handler, "{\"op\":0}").status());
        assertEquals(400, handle(handler, "not json").status()); assertTrue(events.isEmpty());
    }
}
