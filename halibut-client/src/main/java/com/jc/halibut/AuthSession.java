package com.jc.halibut;

import com.google.gwt.storage.client.Storage;

public final class AuthSession {
    private static final AuthSession INSTANCE = new AuthSession();

    private static final String KEY_USER_ID = "auth.userId";
    private static final String KEY_SESSION_ID = "auth.sessionId";
    private static final String KEY_SECURITY_TOKEN = "auth.securityToken";
    private static final String KEY_SESSION_EXPIRATION = "auth.sessionExpiration";
    private static final String KEY_DISPLAY_NAME = "auth.displayName";
    private static final String KEY_AUTO_RESTORE = "auth.autoRestore";
    private static final String KEY_ROLE = "auth.role";

    private Long userId;
    private String sessionId;
    private String securityToken;
    private long sessionExpirationTimestamp;
    private String displayName;
    private boolean autoSessionRestoreEnabled;
    private String role;

    private AuthSession() {
    }

    public static AuthSession getInstance() {
        return INSTANCE;
    }

    public void apply(LoginResponse response) {
        if (response == null) {
            clear();
            return;
        }

        this.userId = response.getUserId();
        this.sessionId = response.getSessionId();
        this.securityToken = response.getSecurityToken();
        this.sessionExpirationTimestamp = response.getSessionExpirationTimestamp();
        this.displayName = response.getDisplayName();
        this.autoSessionRestoreEnabled = response.isAutoSessionRestoreEnabled();
        this.role = response.getRole();
        persistIfNeeded();
    }

    public void loadFromStorageIfEnabled() {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage == null) {
            return;
        }

        boolean storedAutoRestore = Boolean.parseBoolean(valueOrEmpty(storage.getItem(KEY_AUTO_RESTORE)));
        if (!storedAutoRestore) {
            return;
        }

        String rawUserId = storage.getItem(KEY_USER_ID);
        if (rawUserId == null || rawUserId.trim().isEmpty()) {
            return;
        }

        this.userId = Long.parseLong(rawUserId);
        this.sessionId = storage.getItem(KEY_SESSION_ID);
        this.securityToken = storage.getItem(KEY_SECURITY_TOKEN);
        this.sessionExpirationTimestamp = Long.parseLong(valueOrDefault(storage.getItem(KEY_SESSION_EXPIRATION), "0"));
        this.displayName = storage.getItem(KEY_DISPLAY_NAME);
        this.autoSessionRestoreEnabled = true;
        this.role = storage.getItem(KEY_ROLE);
    }

    public void updateAutoSessionRestoreEnabled(boolean enabled) {
        this.autoSessionRestoreEnabled = enabled;
        persistIfNeeded();
    }

    public void clear() {
        this.userId = null;
        this.sessionId = null;
        this.securityToken = null;
        this.sessionExpirationTimestamp = 0L;
        this.displayName = null;
        this.autoSessionRestoreEnabled = false;
        this.role = null;
        clearStoredSession();
    }

    public boolean hasSession() {
        return userId != null && notBlank(sessionId) && notBlank(securityToken);
    }

    public boolean isAutoSessionRestoreEnabled() {
        return autoSessionRestoreEnabled;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public long getSessionExpirationTimestamp() {
        return sessionExpirationTimestamp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    private void persistIfNeeded() {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage == null) {
            return;
        }

        if (!autoSessionRestoreEnabled || !hasSession()) {
            clearStoredSession();
            return;
        }

        storage.setItem(KEY_USER_ID, String.valueOf(userId));
        storage.setItem(KEY_SESSION_ID, sessionId);
        storage.setItem(KEY_SECURITY_TOKEN, securityToken);
        storage.setItem(KEY_SESSION_EXPIRATION, String.valueOf(sessionExpirationTimestamp));
        storage.setItem(KEY_DISPLAY_NAME, valueOrEmpty(displayName));
        storage.setItem(KEY_AUTO_RESTORE, "true");
        storage.setItem(KEY_ROLE, valueOrEmpty(role));
    }

    private void clearStoredSession() {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage == null) {
            return;
        }

        storage.removeItem(KEY_USER_ID);
        storage.removeItem(KEY_SESSION_ID);
        storage.removeItem(KEY_SECURITY_TOKEN);
        storage.removeItem(KEY_SESSION_EXPIRATION);
        storage.removeItem(KEY_DISPLAY_NAME);
        storage.removeItem(KEY_AUTO_RESTORE);
        storage.removeItem(KEY_ROLE);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
