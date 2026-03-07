package com.jc.halibut;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import com.google.inject.Inject;
import com.jc.halibut.Entity.ActiveSession;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.auth.ActiveSessionRepository;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.auth.ServerInjector;
import jakarta.servlet.http.Cookie;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
    private static final String DEVICE_COOKIE_NAME = "device_id";
    private static final Duration SESSION_TTL = Duration.ofHours(8);

    private final LoginAccountRepository accountRepository;
    private final ActiveSessionRepository activeSessionRepository;

    public LoginServiceImpl() {
        this(
                ServerInjector.getInjector().getInstance(LoginAccountRepository.class),
                ServerInjector.getInjector().getInstance(ActiveSessionRepository.class)
        );
    }

    @Inject
    public LoginServiceImpl(LoginAccountRepository accountRepository, ActiveSessionRepository activeSessionRepository) {
        this.accountRepository = accountRepository;
        this.activeSessionRepository = activeSessionRepository;
    }

    @Override
    public LoginResponse login(String username, String password) throws IllegalArgumentException {
        String normalizedUser = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        if (normalizedUser.isEmpty() || normalizedPassword.isEmpty()) {
            return new LoginResponse(false, "Username and password are required.", null);
        }

        Optional<LoginAccount> account = accountRepository.findActiveByCredentials(normalizedUser, normalizedPassword);
        if (account.isEmpty()) {
            return new LoginResponse(false, "Invalid credentials.", null);
        }

        LoginAccount matched = account.get();
        String deviceId = resolveOrCreateDeviceId();

        ActiveSession activeSession = activeSessionRepository.createAuthenticatedSession(
                matched.getId(),
                deviceId,
                SESSION_TTL
        );

        return new LoginResponse(
                true,
                "Login successful.",
                matched.getDisplayName(),
                matched.getId(),
                activeSession.getSessionId(),
                activeSession.getSecurityToken(),
                activeSession.getSessionExpirationTimestamp().toEpochMilli(),
                matched.isAutoSessionRestoreEnabled(),
                matched.getRole().name()
        );
    }

    @Override
    public boolean validateSession(Long userId, String sessionId, String securityToken) throws IllegalArgumentException {
        return activeSessionRepository.isSessionActive(userId, sessionId, securityToken);
    }

    @Override
    public boolean deactivateSession(Long userId, String sessionId, String securityToken) throws IllegalArgumentException {
        return activeSessionRepository.deactivateSession(userId, sessionId, securityToken);
    }

    @Override
    public boolean updateAutoSessionRestorePreference(Long userId, String sessionId, String securityToken, boolean enabled)
            throws IllegalArgumentException {
        boolean validSession = activeSessionRepository.isSessionActive(userId, sessionId, securityToken);
        if (!validSession) {
            return false;
        }
        return accountRepository.updateAutoSessionRestorePreference(userId, enabled);
    }

    private String resolveOrCreateDeviceId() {
        Cookie[] cookies = getThreadLocalRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (DEVICE_COOKIE_NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        String newDeviceId = UUID.randomUUID().toString().replace("-", "");
        Cookie cookie = new Cookie(DEVICE_COOKIE_NAME, newDeviceId);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        getThreadLocalResponse().addCookie(cookie);
        return newDeviceId;
    }
}
