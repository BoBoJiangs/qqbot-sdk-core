package top.sshh.qqbot.sdk;

/** Machine-readable failures. Request bodies, credentials and tokens are never included. */
public final class QQApiException extends RuntimeException {
    public enum Kind { AUTHENTICATION, RATE_LIMIT, REMOTE, TRANSPORT, UNKNOWN_RESULT, INVALID_RESPONSE, REPLY_LIMIT, GATEWAY_FATAL }
    private final Kind kind;
    private final int httpStatus;
    private final Long platformCode;
    private final String traceId;
    private final String retryAfter;

    public QQApiException(Kind kind, int httpStatus, Long platformCode, String traceId, String retryAfter) {
        super("QQ API " + kind + " (HTTP " + httpStatus + ", code=" + platformCode + ")");
        this.kind = kind; this.httpStatus = httpStatus; this.platformCode = platformCode;
        this.traceId = traceId; this.retryAfter = retryAfter;
    }
    public QQApiException(Kind kind) { this(kind, 0, null, null, null); }
    public Kind kind() { return kind; }
    public int httpStatus() { return httpStatus; }
    public Long platformCode() { return platformCode; }
    public String traceId() { return traceId; }
    public String retryAfter() { return retryAfter; }
    public boolean mayHaveSucceeded() { return kind == Kind.UNKNOWN_RESULT; }
}
