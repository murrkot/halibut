package com.jc.halibut.location;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.jc.halibut.dto.LocationDto;

import java.util.List;

public interface LocationServiceAsync {
    void getLocations(Long userId, String sessionId, String securityToken, AsyncCallback<List<LocationDto>> callback);

    void getLocationById(Long userId, String sessionId, String securityToken, Long locationId,
                         AsyncCallback<LocationDto> callback);

    void saveLocation(Long userId, String sessionId, String securityToken, LocationDto location,
                      AsyncCallback<Boolean> callback);

    void deleteLocation(Long userId, String sessionId, String securityToken, Long locationId,
                        AsyncCallback<Boolean> callback);
}
