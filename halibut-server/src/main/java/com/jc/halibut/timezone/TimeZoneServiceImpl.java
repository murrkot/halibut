package com.jc.halibut.timezone;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class TimeZoneServiceImpl extends RemoteServiceServlet implements TimeZoneService {
    private static final List<String> TIME_ZONE_IDS = buildTimeZoneIds();

    public TimeZoneServiceImpl() {
    }

    @Override
    public List<String> getTimeZoneIds(Long userId, String sessionId, String securityToken)
            throws IllegalArgumentException {
        return new ArrayList<>(TIME_ZONE_IDS);
    }

    private static List<String> buildTimeZoneIds() {
        List<String> ids = new ArrayList<>();
        for (String id : ZoneId.getAvailableZoneIds()) {
            if ("UTC".equals(id) || id.contains("/")) {
                ids.add(id);
            }
        }
        Collections.sort(ids);
        return ids;
    }
}
