package top.sshh.qqbot.sdk;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.NamedParameterSpec;
import java.util.HexFormat;
import java.util.Map;

/** Ed25519 verification over timestamp + original bytes, per QQ's published vectors. */
public final class WebhookVerifier {
    private final KeyPair keys;
    public WebhookVerifier(String secret) {
        if (secret == null || secret.isBlank()) throw new IllegalArgumentException("AppSecret is required");
        byte[] source = secret.getBytes(StandardCharsets.UTF_8), seed = new byte[32];
        for (int i = 0; i < seed.length; i++) seed[i] = source[i % source.length];
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
            generator.initialize(NamedParameterSpec.ED25519, new SecureRandom() {
                private int position;
                @Override public void nextBytes(byte[] bytes) {
                    if (position + bytes.length > seed.length) throw new IllegalStateException("Unexpected Ed25519 provider seed consumption");
                    System.arraycopy(seed, position, bytes, 0, bytes.length); position += bytes.length;
                }
            });
            keys = generator.generateKeyPair();
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Ed25519 is unavailable", e); }
    }
    public boolean verify(String timestamp, String hexSignature, byte[] originalBody) {
        if (timestamp == null || timestamp.isBlank() || hexSignature == null || originalBody == null) return false;
        try {
            byte[] signature = HexFormat.of().parseHex(hexSignature);
            if (signature.length != 64 || (signature[63] & 224) != 0) return false;
            Signature verifier = Signature.getInstance("Ed25519"); verifier.initVerify(keys.getPublic());
            verifier.update(timestamp.getBytes(StandardCharsets.UTF_8)); verifier.update(originalBody);
            return verifier.verify(signature);
        } catch (GeneralSecurityException | IllegalArgumentException e) { return false; }
    }
    public Map<String, String> validationResponse(String timestamp, String plainToken) {
        if (timestamp == null || plainToken == null) throw new IllegalArgumentException("Validation fields are required");
        try {
            Signature signer = Signature.getInstance("Ed25519"); signer.initSign(keys.getPrivate());
            signer.update((timestamp + plainToken).getBytes(StandardCharsets.UTF_8));
            return Map.of("plain_token", plainToken, "signature", HexFormat.of().formatHex(signer.sign()));
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Ed25519 signing failed", e); }
    }
}
