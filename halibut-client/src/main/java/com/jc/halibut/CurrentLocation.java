package com.jc.halibut;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;
import com.jc.halibut.dto.LocationDto;

import java.util.ArrayList;
import java.util.List;

public final class CurrentLocation {
    public interface Listener {
        void onLocationChanged(LocationDto location);
    }

    public static final String COOKIE_LOCATION_ID = "halibut_location_id";
    public static final String COOKIE_LOCATION_NAME = "halibut_location_name";

    private static final CurrentLocation INSTANCE = new CurrentLocation();

    private final List<Listener> listeners = new ArrayList<>();
    private LocationDto current;

    private CurrentLocation() {
    }

    public static CurrentLocation getInstance() {
        return INSTANCE;
    }

    public void loadFromCookies() {
        String rawId = Cookies.getCookie(COOKIE_LOCATION_ID);
        String rawName = Cookies.getCookie(COOKIE_LOCATION_NAME);

        if (rawId == null || rawId.trim().isEmpty()) {
            return;
        }

        Long id;
        try {
            id = Long.parseLong(rawId.trim());
        } catch (NumberFormatException ex) {
            return;
        }

        LocationDto dto = new LocationDto();
        dto.setId(id);
        dto.setName(rawName == null ? "" : URL.decodeQueryString(rawName));
        setCurrent(dto);
    }

    public void setCurrent(LocationDto location) {
        if (location == null) {
            this.current = null;
            notifyListeners();
            return;
        }

        LocationDto copy = new LocationDto();
        copy.setId(location.getId());
        copy.setName(location.getName());
        copy.setDescription(location.getDescription());
        this.current = copy;
        notifyListeners();
    }

    public LocationDto getCurrent() {
        if (current == null) {
            return null;
        }

        LocationDto copy = new LocationDto();
        copy.setId(current.getId());
        copy.setName(current.getName());
        copy.setDescription(current.getDescription());
        return copy;
    }

    public String getCurrentName() {
        return current == null || current.getName() == null ? "" : current.getName();
    }

    public void addListener(Listener listener) {
        if (listener == null || listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Listener listener : new ArrayList<>(listeners)) {
            listener.onLocationChanged(getCurrent());
        }
    }
}
