package com.jc.halibut.location;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import com.google.inject.Inject;
import com.jc.halibut.Entity.Location;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import com.jc.halibut.auth.ActiveSessionRepository;
import com.jc.halibut.auth.AuditEventRepository;
import com.jc.halibut.auth.AuditEventType;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.auth.ServerInjector;
import com.jc.halibut.dto.LocationDto;
import jakarta.servlet.http.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

@SuppressWarnings("serial")
public class LocationServiceImpl extends RemoteServiceServlet implements LocationService {
    private final ActiveSessionRepository activeSessionRepository;
    private final LoginAccountRepository accountRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final AuditEventRepository auditEventRepository;

    public LocationServiceImpl() {
        this(
                ServerInjector.getInjector().getInstance(ActiveSessionRepository.class),
                ServerInjector.getInjector().getInstance(LoginAccountRepository.class),
                ServerInjector.getInjector().getInstance(LocationRepository.class),
                ServerInjector.getInjector().getInstance(LocationMapper.class),
                ServerInjector.getInjector().getInstance(AuditEventRepository.class)
        );
    }

    @Inject
    public LocationServiceImpl(ActiveSessionRepository activeSessionRepository,
                               LoginAccountRepository accountRepository,
                               LocationRepository locationRepository,
                               LocationMapper locationMapper,
                               AuditEventRepository auditEventRepository) {
        this.activeSessionRepository = activeSessionRepository;
        this.accountRepository = accountRepository;
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public List<LocationDto> getLocations(Long userId, String sessionId, String securityToken) throws IllegalArgumentException {
        if (!isAuthorizedForLocationManagement(userId, sessionId, securityToken)) {
            return new ArrayList<>();
        }
        return locationMapper.toDtoList(locationRepository.findAll());
    }

    @Override
    public LocationDto getLocationById(Long userId, String sessionId, String securityToken, Long locationId)
            throws IllegalArgumentException {
        if (!isAuthorizedForLocationManagement(userId, sessionId, securityToken)) {
            return null;
        }
        LocationDto location = locationMapper.toDto(locationRepository.findById(locationId));
        updateSessionLocation(userId, sessionId, securityToken, location);
        return location;
    }

    @Override
    public boolean saveLocation(Long userId, String sessionId, String securityToken, LocationDto location)
            throws IllegalArgumentException {
        if (!isAuthorizedForLocationManagement(userId, sessionId, securityToken)) {
            return false;
        }
        return locationRepository.saveLocation(location);
    }

    @Override
    public boolean deleteLocation(Long userId, String sessionId, String securityToken, Long locationId)
            throws IllegalArgumentException {
        if (!isAuthorizedForLocationManagement(userId, sessionId, securityToken)) {
            return false;
        }
        return locationRepository.deleteLocation(locationId);
    }

    private void updateSessionLocation(Long userId, String sessionId, String securityToken, LocationDto location) {
        if (location == null) {
            return;
        }
        String previousLocation = resolveLocationNameFromCookies();

        activeSessionRepository.updateSessionLocation(
                userId,
                sessionId,
                securityToken,
                location.getId(),
                location.getName()
        );
        persistLocationCookie(location);
        String newLocationLabel = safeLocationLabel(location);
        String newLocationName = location.getName() == null ? "" : location.getName().trim();
        boolean hadPrevious = previousLocation != null && !previousLocation.trim().isEmpty()
                && !"Location not set".equalsIgnoreCase(previousLocation.trim());
        String locationName = newLocationName;
        String details = hadPrevious
                ? "Previous location: " + previousLocation.trim() + " | New location: " + newLocationLabel
                : "Location set: " + newLocationLabel;

        auditEventRepository.logEvent(
                AuditEventType.LOCATION_SET,
                userId,
                resolveUserName(userId),
                sessionId,
                resolveRemoteAddress(),
                true,
                details,
                locationName
        );
    }

    private void persistLocationCookie(LocationDto location) {
        if (location == null || location.getId() == null || location.getName() == null) {
            return;
        }
        if (getThreadLocalResponse() == null) {
            return;
        }
        Cookie idCookie = new Cookie("halibut_location_id", String.valueOf(location.getId()));
        idCookie.setPath("/");
        idCookie.setMaxAge(60 * 60 * 24 * 365 * 20);

        String encodedName = URLEncoder.encode(location.getName().trim(), StandardCharsets.UTF_8);
        Cookie nameCookie = new Cookie("halibut_location_name", encodedName);
        nameCookie.setPath("/");
        nameCookie.setMaxAge(60 * 60 * 24 * 365 * 20);

        getThreadLocalResponse().addCookie(idCookie);
        getThreadLocalResponse().addCookie(nameCookie);
    }

    private String safeLocationLabel(LocationDto location) {
        if (location == null) {
            return "";
        }
        String name = location.getName() == null ? "" : location.getName().trim();
        Long id = location.getId();
        if (!name.isEmpty() && id != null) {
            return name + " (id=" + id + ")";
        }
        return !name.isEmpty() ? name : (id == null ? "" : String.valueOf(id));
    }

    private String resolveLocationNameFromCookies() {
        Cookie[] cookies = null;
        try {
            cookies = getThreadLocalRequest().getCookies();
        } catch (RuntimeException ex) {
            return "Location not set";
        }
        if (cookies == null || cookies.length == 0) {
            return "Location not set";
        }

        String rawId = null;
        for (Cookie cookie : cookies) {
            if (cookie == null) {
                continue;
            }
            if ("halibut_location_id".equals(cookie.getName())) {
                rawId = cookie.getValue();
                break;
            }
        }

        Long id = parseLocationId(rawId);
        if (id == null) {
            return "Location not set";
        }
        Location found = locationRepository.findById(id);
        if (found == null || found.getName() == null || found.getName().trim().isEmpty()) {
            return "Location not set";
        }
        return found.getName().trim();
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

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return "";
        }
        Optional<LoginAccount> requester = accountRepository.findById(userId);
        if (requester.isPresent() && requester.get().getUsername() != null) {
            return requester.get().getUsername();
        }
        return "";
    }

    private String resolveRemoteAddress() {
        try {
            return getThreadLocalRequest().getRemoteAddr();
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private boolean isAuthorizedForLocationManagement(Long userId, String sessionId, String securityToken) {
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
}
