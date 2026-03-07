package com.jc.halibut;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {
    void login(String username, String password, AsyncCallback<LoginResponse> callback);

    void validateSession(Long userId, String sessionId, String securityToken, AsyncCallback<Boolean> callback);

    void deactivateSession(Long userId, String sessionId, String securityToken, AsyncCallback<Boolean> callback);

    void updateAutoSessionRestorePreference(Long userId, String sessionId, String securityToken, boolean enabled,
                                            AsyncCallback<Boolean> callback);
}
