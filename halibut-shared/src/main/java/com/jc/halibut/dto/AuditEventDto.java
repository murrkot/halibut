package com.jc.halibut.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AuditEventDto implements Serializable {
    private Long id;
    private long eventTime;
    private String eventType;
    private Long userId;
    private String userName;
    private String sessionId;
    private String remoteAddress;
    private boolean success;
    private String details;

    public AuditEventDto() {
    }

    public AuditEventDto(Long id, long eventTime, String eventType, Long userId, String userName,
                         String sessionId, String remoteAddress, boolean success, String details) {
        this.id = id;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.userId = userId;
        this.userName = userName;
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.success = success;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
