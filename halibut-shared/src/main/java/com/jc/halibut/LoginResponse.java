package com.jc.halibut;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LoginResponse implements Serializable {
    private boolean success;
    private String message;
    private String displayName;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message, String displayName) {
        this.success = success;
        this.message = message;
        this.displayName = displayName;
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
}
