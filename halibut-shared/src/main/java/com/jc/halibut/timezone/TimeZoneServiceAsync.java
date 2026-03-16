package com.jc.halibut.timezone;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface TimeZoneServiceAsync {
    void getTimeZoneIds(Long userId, String sessionId, String securityToken, AsyncCallback<List<String>> callback);
}
