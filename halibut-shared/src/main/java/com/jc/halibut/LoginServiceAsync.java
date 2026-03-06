package com.jc.halibut;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {
    void login(String username, String password, AsyncCallback<LoginResponse> callback);
}
