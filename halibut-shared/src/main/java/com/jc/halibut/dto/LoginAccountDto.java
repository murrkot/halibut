package com.jc.halibut.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LoginAccountDto implements Serializable {
    private Long id;
    private String username;
    private String displayName;
    private LoginAccountRole role;
    private boolean autoSessionRestoreEnabled;
    private boolean active;
    private String plainPassword;
    private String sessionTimeout;

    public LoginAccountDto() {
    }

    public LoginAccountDto(Long id, String username, String displayName, LoginAccountRole role,
                           boolean autoSessionRestoreEnabled, boolean active) {
        this(id, username, displayName, role, autoSessionRestoreEnabled, active, null);
    }

    public LoginAccountDto(Long id, String username, String displayName, LoginAccountRole role,
                           boolean autoSessionRestoreEnabled, boolean active, String sessionTimeout) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.autoSessionRestoreEnabled = autoSessionRestoreEnabled;
        this.active = active;
        this.sessionTimeout = sessionTimeout;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LoginAccountRole getRole() {
        return role;
    }

    public void setRole(LoginAccountRole role) {
        this.role = role;
    }

    public boolean isAutoSessionRestoreEnabled() {
        return autoSessionRestoreEnabled;
    }

    public void setAutoSessionRestoreEnabled(boolean autoSessionRestoreEnabled) {
        this.autoSessionRestoreEnabled = autoSessionRestoreEnabled;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
