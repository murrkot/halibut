package com.jc.halibut;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LoginResponse implements Serializable {
    private boolean success;
    private String message;
    private String displayName;
    private Long userId;
    private String sessionId;
    private String securityToken;
    private long sessionExpirationTimestamp;
    private boolean autoSessionRestoreEnabled;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message, String displayName) {
        this.success = success;
        this.message = message;
        this.displayName = displayName;
    }

    public LoginResponse(boolean success, String message, String displayName, Long userId, String sessionId,
                         String securityToken, long sessionExpirationTimestamp, boolean autoSessionRestoreEnabled,
                         String role) {
        this.success = success;
        this.message = message;
        this.displayName = displayName;
        this.userId = userId;
        this.sessionId = sessionId;
        this.securityToken = securityToken;
        this.sessionExpirationTimestamp = sessionExpirationTimestamp;
        this.autoSessionRestoreEnabled = autoSessionRestoreEnabled;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public long getSessionExpirationTimestamp() {
        return sessionExpirationTimestamp;
    }

    public void setSessionExpirationTimestamp(long sessionExpirationTimestamp) {
        this.sessionExpirationTimestamp = sessionExpirationTimestamp;
    }

    public boolean isAutoSessionRestoreEnabled() {
        return autoSessionRestoreEnabled;
    }

    public void setAutoSessionRestoreEnabled(boolean autoSessionRestoreEnabled) {
        this.autoSessionRestoreEnabled = autoSessionRestoreEnabled;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
