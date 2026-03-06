package com.jc.halibut;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
    LoginResponse login(String username, String password) throws IllegalArgumentException;
}
