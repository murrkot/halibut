package com.jc.halibut;

import java.util.Optional;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import com.google.inject.Inject;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.auth.ServerInjector;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
    private final LoginAccountRepository repository;

    public LoginServiceImpl() {
        this(ServerInjector.getInjector().getInstance(LoginAccountRepository.class));
    }

    @Inject
    public LoginServiceImpl(LoginAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoginResponse login(String username, String password) throws IllegalArgumentException {
        String normalizedUser = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        if (normalizedUser.isEmpty() || normalizedPassword.isEmpty()) {
            return new LoginResponse(false, "Username and password are required.", null);
        }

        Optional<LoginAccount> account = repository.findActiveByCredentials(normalizedUser, normalizedPassword);
        if (account.isEmpty()) {
            return new LoginResponse(false, "Invalid credentials.", null);
        }

        LoginAccount matched = account.get();
        return new LoginResponse(true, "Login successful.", matched.getDisplayName());
    }
}
