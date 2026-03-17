package com.jc.halibut.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class LoginAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 64)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginRole role = LoginRole.USER;

    @Column(name = "auto_session_restore_enabled", nullable = false)
    private boolean autoSessionRestoreEnabled = false;

    @Column(name = "session_timeout", nullable = false, length = 16)
    private String sessionTimeout = "30m";

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LoginRole getRole() {
        return role;
    }

    public void setRole(LoginRole role) {
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

    public String getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
