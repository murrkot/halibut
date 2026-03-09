package com.jc.halibut.location;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import com.google.inject.Inject;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import com.jc.halibut.auth.ActiveSessionRepository;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.auth.ServerInjector;
import com.jc.halibut.dto.LocationDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("serial")
public class LocationServiceImpl extends RemoteServiceServlet implements LocationService {
    private final ActiveSessionRepository activeSessionRepository;
    private final LoginAccountRepository accountRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationServiceImpl() {
        this(
                ServerInjector.getInjector().getInstance(ActiveSessionRepository.class),
                ServerInjector.getInjector().getInstance(LoginAccountRepository.class),
                ServerInjector.getInjector().getInstance(LocationRepository.class),
                ServerInjector.getInjector().getInstance(LocationMapper.class)
        );
    }

    @Inject
    public LocationServiceImpl(ActiveSessionRepository activeSessionRepository,
                               LoginAccountRepository accountRepository,
                               LocationRepository locationRepository,
                               LocationMapper locationMapper) {
        this.activeSessionRepository = activeSessionRepository;
        this.accountRepository = accountRepository;
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
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
        return locationMapper.toDto(locationRepository.findById(locationId));
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
