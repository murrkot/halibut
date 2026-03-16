package com.jc.halibut.timezone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-abc";
    private static final String SECURITY_TOKEN = "token-xyz";

    private TimeZoneServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TimeZoneServiceImpl();
    }

    @Test
    void returnsSortedTimeZones() {
        List<String> result = service.getTimeZoneIds(USER_ID, SESSION_ID, SECURITY_TOKEN);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("UTC"));

        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).compareTo(result.get(i)) <= 0);
        }

        for (String id : result) {
            assertTrue("UTC".equals(id) || id.contains("/"));
        }
    }
}
