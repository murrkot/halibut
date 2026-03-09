package com.jc.halibut;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.jc.halibut.dto.LoginAccountDto;

import java.util.List;

public interface LoginServiceAsync {
    void login(String username, String password, AsyncCallback<LoginResponse> callback);

    void validateSession(Long userId, String sessionId, String securityToken, AsyncCallback<Boolean> callback);

    void deactivateSession(Long userId, String sessionId, String securityToken, AsyncCallback<Boolean> callback);

    void updateAutoSessionRestorePreference(Long userId, String sessionId, String securityToken, boolean enabled,
                                            AsyncCallback<Boolean> callback);

    void getLoginAccounts(Long userId, String sessionId, String securityToken, AsyncCallback<List<LoginAccountDto>> callback);

    void saveLoginAccount(Long userId, String sessionId, String securityToken, LoginAccountDto account,
                          AsyncCallback<Boolean> callback);

    void deleteLoginAccount(Long userId, String sessionId, String securityToken, Long accountId,
                            AsyncCallback<Boolean> callback);
}
