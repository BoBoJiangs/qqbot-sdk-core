package top.sshh.qqbot.sdk;

/** Credentials seam for applications with their own token store. */
public interface AccessTokenProvider {
    String getToken();
    void invalidateToken();
    default String refreshRejected(String rejectedToken) {
        invalidateToken();
        return getToken();
    }
}
