package com.jc.halibut.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "active_session")
public class ActiveSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(name = "session_expiration_timestamp", nullable = false)
    private Instant sessionExpirationTimestamp;

    @Column(name = "security_token", nullable = false, unique = true, length = 128)
    private String securityToken;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    public Long getId() {
        return id;
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

    public Instant getSessionExpirationTimestamp() {
        return sessionExpirationTimestamp;
    }

    public void setSessionExpirationTimestamp(Instant sessionExpirationTimestamp) {
        this.sessionExpirationTimestamp = sessionExpirationTimestamp;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
