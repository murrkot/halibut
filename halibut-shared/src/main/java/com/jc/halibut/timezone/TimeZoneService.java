package com.jc.halibut.timezone;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

@RemoteServiceRelativePath("timezone")
public interface TimeZoneService extends RemoteService {
    List<String> getTimeZoneIds(Long userId, String sessionId, String securityToken) throws IllegalArgumentException;
}
