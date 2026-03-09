package com.jc.halibut.location;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.jc.halibut.dto.LocationDto;

import java.util.List;

@RemoteServiceRelativePath("location")
public interface LocationService extends RemoteService {
    List<LocationDto> getLocations(Long userId, String sessionId, String securityToken) throws IllegalArgumentException;

    LocationDto getLocationById(Long userId, String sessionId, String securityToken, Long locationId)
            throws IllegalArgumentException;

    boolean saveLocation(Long userId, String sessionId, String securityToken, LocationDto location)
            throws IllegalArgumentException;

    boolean deleteLocation(Long userId, String sessionId, String securityToken, Long locationId)
            throws IllegalArgumentException;
}
