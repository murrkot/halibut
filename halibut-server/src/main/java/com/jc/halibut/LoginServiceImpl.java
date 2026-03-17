package com.jc.halibut;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import com.google.inject.Inject;
import com.jc.halibut.Entity.ActiveSession;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import com.jc.halibut.auth.ActiveSessionRepository;
import com.jc.halibut.auth.AuditEventRepository;
import com.jc.halibut.auth.AuditEventType;
import com.jc.halibut.auth.LoginAccountMapper;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.auth.SessionCleanupService;
import com.jc.halibut.auth.ServerInjector;
import com.jc.halibut.dto.LoginAccountDto;
import com.jc.halibut.location.LocationRepository;
import jakarta.servlet.http.Cookie;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
    private static final String DEVICE_COOKIE_NAME = "device_id";
    private static final String LOCATION_ID_COOKIE = "halibut_location_id";
    private static final String LOCATION_NAME_COOKIE = "halibut_location_name";
    private static final Duration DEFAULT_SESSION_TTL = Duration.ofHours(8);

    private final LoginAccountRepository accountRepository;
    private final ActiveSessionRepository activeSessionRepository;
    private final LoginAccountMapper loginAccountMapper;
    private final AuditEventRepository auditEventRepository;
    private final LocationRepository locationRepository;

    public LoginServiceImpl() {
        this(
                ServerInjector.getInjector().getInstance(LoginAccountRepository.class),
                ServerInjector.getInjector().getInstance(ActiveSessionRepository.class),
                ServerInjector.getInjector().getInstance(LoginAccountMapper.class),
                ServerInjector.getInjector().getInstance(AuditEventRepository.class),
                ServerInjector.getInjector().getInstance(LocationRepository.class)
        );
    }

    @Inject
    public LoginServiceImpl(LoginAccountRepository accountRepository,
                            ActiveSessionRepository activeSessionRepository,
                            LoginAccountMapper loginAccountMapper,
                            AuditEventRepository auditEventRepository,
                            LocationRepository locationRepository) {
        this.accountRepository = accountRepository;
        this.activeSessionRepository = activeSessionRepository;
        this.loginAccountMapper = loginAccountMapper;
        this.auditEventRepository = auditEventRepository;
        this.locationRepository = locationRepository;
        SessionCleanupService.ensureStarted(activeSessionRepository);
    }

    @Override
    public LoginResponse login(String username, String password) throws IllegalArgumentException {
        String normalizedUser = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        if (normalizedUser.isEmpty() || normalizedPassword.isEmpty()) {
            LocationSnapshot snapshot = resolveLocationFromCookies();
            auditEventRepository.logEvent(
                    AuditEventType.WRONG_PASSWORD,
                    null,
                    normalizedUser,
                    null,
                    resolveRemoteAddress(),
                    false,
                    appendLocationDetails("Missing username or password", snapshot.locationLabel()),
                    snapshot.locationName());
            return new LoginResponse(false, "Username and password are required.", null);
        }

        Optional<LoginAccount> account = accountRepository.findActiveByCredentials(normalizedUser, normalizedPassword);
        if (account.isEmpty()) {
            LocationSnapshot snapshot = resolveLocationFromCookies();
            auditEventRepository.logEvent(
                    AuditEventType.WRONG_PASSWORD,
                    null,
                    normalizedUser,
                    null,
                    resolveRemoteAddress(),
                    false,
                    appendLocationDetails("Invalid credentials", snapshot.locationLabel()),
                    snapshot.locationName());
            return new LoginResponse(false, "Invalid credentials.", null);
        }

        LoginAccount matched = account.get();
        String deviceId = resolveOrCreateDeviceId();

        Duration sessionTtl = resolveSessionTtl(matched);
        LocationSnapshot locationSnapshot = resolveLocationFromCookies();
        ActiveSession activeSession = activeSessionRepository.createAuthenticatedSession(
                matched.getId(),
                deviceId,
                locationSnapshot.locationId(),
                locationSnapshot.locationName(),
                sessionTtl
        );

        auditEventRepository.logEvent(
                AuditEventType.LOGIN,
                matched.getId(),
                matched.getUsername(),
                activeSession.getSessionId(),
                resolveRemoteAddress(),
                true,
                appendLocationDetails("Login successful", locationSnapshot.locationLabel()),
                locationSnapshot.locationName());

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
        ActiveSessionRepository.SessionStatus status =
                activeSessionRepository.getSessionStatus(userId, sessionId, securityToken);
        LocationSnapshot snapshot = resolveLocationFromCookies();
        if (status == ActiveSessionRepository.SessionStatus.EXPIRED) {
            auditEventRepository.logEvent(
                    AuditEventType.TIMEOUT,
                    userId,
                    resolveUserName(userId, null),
                    sessionId,
                    resolveRemoteAddress(),
                    false,
                    appendLocationDetails("Session expired", snapshot.locationLabel()),
                    snapshot.locationName());
        }
        if (status == ActiveSessionRepository.SessionStatus.ACTIVE) {
            syncSessionLocationFromCookies(userId, sessionId, securityToken);
            ensureLoginAuditRecorded(userId, sessionId, securityToken);
        }
        return status == ActiveSessionRepository.SessionStatus.ACTIVE;
    }

    @Override
    public boolean deactivateSession(Long userId, String sessionId, String securityToken) throws IllegalArgumentException {
        LocationSnapshot snapshot = resolveLocationFromCookies();
        boolean result = activeSessionRepository.deactivateSession(userId, sessionId, securityToken);
        if (result) {
            auditEventRepository.logEvent(
                    AuditEventType.LOGOUT,
                    userId,
                    resolveUserName(userId, null),
                    sessionId,
                    resolveRemoteAddress(),
                    true,
                    appendLocationDetails("Logout", snapshot.locationLabel()),
                    snapshot.locationName());
        }
        return result;
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

    @Override
    public List<LoginAccountDto> getLoginAccounts(Long userId, String sessionId, String securityToken) throws IllegalArgumentException {
        if (!isAuthorizedForUserManagement(userId, sessionId, securityToken)) {
            return new ArrayList<>();
        }
        return loginAccountMapper.toDtoList(accountRepository.findAllActive());
    }

    @Override
    public boolean saveLoginAccount(Long userId, String sessionId, String securityToken, LoginAccountDto account)
            throws IllegalArgumentException {
        if (!isAuthorizedForUserManagement(userId, sessionId, securityToken)) {
            return false;
        }
        return accountRepository.saveLoginAccount(account);
    }

    @Override
    public boolean deleteLoginAccount(Long userId, String sessionId, String securityToken, Long accountId)
            throws IllegalArgumentException {
        if (!isAuthorizedForUserManagement(userId, sessionId, securityToken)) {
            return false;
        }
        return accountRepository.deleteLoginAccount(accountId);
    }

    @Override
    public boolean changePassword(Long userId, String sessionId, String securityToken, Long accountId,
                                  String newPassword) throws IllegalArgumentException {
        if (!isAuthorizedForUserManagement(userId, sessionId, securityToken)) {
            return false;
        }
        return accountRepository.changePassword(accountId, newPassword);
    }

    @Override
    public boolean changeOwnPassword(Long userId, String sessionId, String securityToken,
                                     String currentPassword, String newPassword) throws IllegalArgumentException {
        boolean validSession = activeSessionRepository.isSessionActive(userId, sessionId, securityToken);
        if (!validSession) {
            return false;
        }
        return accountRepository.changeOwnPassword(userId, currentPassword, newPassword);
    }

    private boolean isAuthorizedForUserManagement(Long userId, String sessionId, String securityToken) {
        boolean validSession = activeSessionRepository.isSessionActive(userId, sessionId, securityToken);
        if (!validSession) {
            return false;
        }

        Optional<LoginAccount> requester = accountRepository.findById(userId);
        if (requester.isEmpty()) {
            return false;
        }

        LoginRole requesterRole = requester.get().getRole();
        return requesterRole != LoginRole.USER;
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

    private String resolveRemoteAddress() {
        try {
            return getThreadLocalRequest().getRemoteAddr();
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private Duration resolveSessionTtl(LoginAccount account) {
        if (account == null) {
            return DEFAULT_SESSION_TTL;
        }

        String raw = account.getSessionTimeout();
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_SESSION_TTL;
        }

        String normalized = raw.trim().toLowerCase();
        if ("1h".equals(normalized)) {
            return Duration.ofHours(1);
        }
        if (normalized.endsWith("m")) {
            String minutesPart = normalized.substring(0, normalized.length() - 1);
            try {
                int minutes = Integer.parseInt(minutesPart);
                if (minutes >= 1 && minutes <= 60) {
                    return Duration.ofMinutes(minutes);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if ("60m".equals(normalized)) {
            return Duration.ofHours(1);
        }

        return DEFAULT_SESSION_TTL;
    }

    private String resolveUserName(Long userId, String fallback) {
        if (userId == null) {
            return fallback;
        }
        Optional<LoginAccount> account = accountRepository.findById(userId);
        if (account.isPresent() && account.get().getUsername() != null) {
            return account.get().getUsername();
        }
        return fallback;
    }

    private void ensureLoginAuditRecorded(Long userId, String sessionId, String securityToken) {
        if (userId == null || sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        if (auditEventRepository.hasEventForSession(sessionId, AuditEventType.LOGIN.name())) {
            return;
        }

        String username = resolveUserName(userId, null);
        LocationSnapshot snapshot = resolveLocationFromCookies();

        auditEventRepository.logEvent(
                AuditEventType.LOGIN,
                userId,
                username,
                sessionId,
                resolveRemoteAddress(),
                true,
                appendLocationDetails("Session restored", snapshot.locationLabel()),
                snapshot.locationName());
    }

    private String appendLocationDetails(String details, String location) {
        if (location == null || location.isEmpty()) {
            return details;
        }
        if (details == null || details.trim().isEmpty()) {
            return "Location: " + location;
        }
        return details + " | Location: " + location;
    }

    private void syncSessionLocationFromCookies(Long userId, String sessionId, String securityToken) {
        ActiveSession activeSession = resolveSession(userId, sessionId, securityToken);
        if (activeSession == null) {
            return;
        }

        LocationSnapshot snapshot = resolveLocationFromCookies();
        if (snapshot.locationId() == null && (snapshot.locationName() == null || snapshot.locationName().isEmpty())) {
            return;
        }

        activeSessionRepository.updateSessionLocation(
                userId,
                sessionId,
                securityToken,
                snapshot.locationId(),
                snapshot.locationName()
        );
    }

    private LocationSnapshot resolveLocationFromCookies() {
        Cookie[] cookies = null;
        try {
            cookies = getThreadLocalRequest().getCookies();
        } catch (RuntimeException ex) {
            return LocationSnapshot.notSet();
        }
        if (cookies == null || cookies.length == 0) {
            return LocationSnapshot.notSet();
        }

        String rawId = null;
        String rawName = null;
        for (Cookie cookie : cookies) {
            if (cookie == null) {
                continue;
            }
            if (LOCATION_ID_COOKIE.equals(cookie.getName())) {
                rawId = cookie.getValue();
            } else if (LOCATION_NAME_COOKIE.equals(cookie.getName())) {
                rawName = cookie.getValue();
            }
        }

        Long id = parseLocationId(rawId);
        String name = resolveLocationNameById(id);
        if (name.isEmpty()) {
            name = decodeCookieValue(rawName);
        }
        if (name == null || name.trim().isEmpty()) {
            return LocationSnapshot.notSet();
        }
        return new LocationSnapshot(id, name);
    }

    private String decodeCookieValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    private Long parseLocationId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(rawId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ActiveSession resolveSession(Long userId, String sessionId, String securityToken) {
        return activeSessionRepository.findActiveSession(userId, sessionId, securityToken).orElse(null);
    }

    private String resolveLocationNameById(Long locationId) {
        if (locationId == null) {
            return "";
        }
        var location = locationRepository.findById(locationId);
        if (location == null || location.getName() == null) {
            return "";
        }
        return location.getName().trim();
    }

    private record LocationSnapshot(Long locationId, String locationName) {
        static LocationSnapshot notSet() {
            return new LocationSnapshot(null, "Location not set");
        }

        String locationLabel() {
            if (locationName == null || locationName.trim().isEmpty()) {
                return "";
            }
            if (locationId == null) {
                return locationName;
            }
            return locationName + " (id=" + locationId + ")";
        }
    }
}
