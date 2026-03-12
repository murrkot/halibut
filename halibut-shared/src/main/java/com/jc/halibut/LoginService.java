package com.jc.halibut;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.jc.halibut.dto.LoginAccountDto;

import java.util.List;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
    LoginResponse login(String username, String password) throws IllegalArgumentException;

    boolean validateSession(Long userId, String sessionId, String securityToken) throws IllegalArgumentException;

    boolean deactivateSession(Long userId, String sessionId, String securityToken) throws IllegalArgumentException;

    boolean updateAutoSessionRestorePreference(Long userId, String sessionId, String securityToken, boolean enabled)
            throws IllegalArgumentException;

    List<LoginAccountDto> getLoginAccounts(Long userId, String sessionId, String securityToken) throws IllegalArgumentException;

    boolean saveLoginAccount(Long userId, String sessionId, String securityToken, LoginAccountDto account)
            throws IllegalArgumentException;

    boolean deleteLoginAccount(Long userId, String sessionId, String securityToken, Long accountId)
            throws IllegalArgumentException;

    boolean changePassword(Long userId, String sessionId, String securityToken, Long accountId, String newPassword)
            throws IllegalArgumentException;

    boolean changeOwnPassword(Long userId, String sessionId, String securityToken, String currentPassword,
                              String newPassword) throws IllegalArgumentException;
}
