package com.jc.halibut;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
    LoginResponse login(String username, String password) throws IllegalArgumentException;

    boolean validateSession(Long userId, String sessionId, String securityToken) throws IllegalArgumentException;

    boolean deactivateSession(Long userId, String sessionId, String securityToken) throws IllegalArgumentException;

    boolean updateAutoSessionRestorePreference(Long userId, String sessionId, String securityToken, boolean enabled)
            throws IllegalArgumentException;
}
