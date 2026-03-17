package com.jc.halibut.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LocationDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String timeZoneId;
    private String createdBy;
    private long createdAt;
    private String lastUpdatedBy;
    private long lastUpdatedAt;

    public LocationDto() {
    }

    public LocationDto(Long id, String name, String description) {
        this(id, name, description, null);
    }

    public LocationDto(Long id, String name, String description, String timeZoneId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.timeZoneId = timeZoneId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
