package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.jc.halibut.AuthSession;
import com.jc.halibut.dto.LocationDto;
import com.jc.halibut.timezone.TimeZoneService;
import com.jc.halibut.timezone.TimeZoneServiceAsync;

import java.util.List;

public class LocationPropertiesPanel extends DialogBox {
    private static final UserPropertiesPanelResources RESOURCES = GWT.create(UserPropertiesPanelResources.class);
    private static final String DEFAULT_TIME_ZONE = "UTC";

    public interface SaveHandler {
        void onSave(LocationDto dto);
    }

    private final AuthSession authSession = AuthSession.getInstance();
    private final TimeZoneServiceAsync timeZoneService = GWT.create(TimeZoneService.class);

    private final TextBox nameBox = new TextBox();
    private final TextArea descriptionBox = new TextArea();
    private final ListBox timeZoneRegionBox = new ListBox();
    private final ListBox timeZoneCityBox = new ListBox();
    private final Label timeZoneHintLabel = new Label();
    private final Button saveButton = new Button("Save");
    private final Label messageLabel = new Label();
    private final LocationDto workingCopy;
    private List<String> availableTimeZones = List.of();

    public LocationPropertiesPanel(String title, LocationDto initialValue, SaveHandler saveHandler) {
        super(true, true);
        setText(title == null ? "Location Properties" : title);
        setAnimationEnabled(true);
        setGlassEnabled(true);

        RESOURCES.style().ensureInjected();
        setStyleName(RESOURCES.style().dialog());

        this.workingCopy = copyOf(initialValue);

        FlowPanel root = new FlowPanel();
        root.setStyleName(RESOURCES.style().root());

        root.add(new Label("Name"));
        nameBox.setStylePrimaryName(RESOURCES.style().input());
        nameBox.setText(nullSafe(workingCopy.getName()));
        root.add(nameBox);

        root.add(new Label("Description"));
        descriptionBox.setStylePrimaryName(RESOURCES.style().input());
        descriptionBox.setVisibleLines(4);
        descriptionBox.setCharacterWidth(40);
        descriptionBox.setText(nullSafe(workingCopy.getDescription()));
        root.add(descriptionBox);

        root.add(new Label("Time Zone Region"));
        timeZoneRegionBox.setStylePrimaryName(RESOURCES.style().input());
        timeZoneRegionBox.setEnabled(false);
        timeZoneRegionBox.addItem("Loading...");
        root.add(timeZoneRegionBox);

        root.add(new Label("Time Zone City"));
        timeZoneCityBox.setStylePrimaryName(RESOURCES.style().input());
        timeZoneCityBox.setEnabled(false);
        timeZoneCityBox.addItem("Loading...");
        root.add(timeZoneCityBox);

        timeZoneHintLabel.setStyleName(RESOURCES.style().hint());
        root.add(timeZoneHintLabel);

        messageLabel.setStyleName(RESOURCES.style().message());
        root.add(messageLabel);

        FlowPanel actions = new FlowPanel();
        actions.setStyleName(RESOURCES.style().actions());

        Button cancelButton = new Button("Cancel");

        saveButton.setStylePrimaryName(RESOURCES.style().actionButton());
        cancelButton.setStylePrimaryName(RESOURCES.style().actionButton());
        saveButton.setEnabled(false);

        saveButton.addClickHandler(event -> {
            if (!readForm()) {
                return;
            }
            hide();
            if (saveHandler != null) {
                saveHandler.onSave(copyOf(workingCopy));
            }
        });

        cancelButton.addClickHandler(event -> hide());

        actions.add(saveButton);
        actions.add(cancelButton);
        root.add(actions);

        setWidget(root);
        bindTimeZoneSelection();
        loadTimeZones();
    }

    private boolean readForm() {
        String name = trimToEmpty(nameBox.getText());
        String description = trimToEmpty(descriptionBox.getText());
        String timeZoneId = getSelectedTimeZoneId();

        if (name.isEmpty()) {
            messageLabel.setText("Name is required.");
            return false;
        }
        if (description.isEmpty()) {
            messageLabel.setText("Description is required.");
            return false;
        }
        if (timeZoneId.isEmpty()) {
            messageLabel.setText("Time Zone is required.");
            return false;
        }

        workingCopy.setName(name);
        workingCopy.setDescription(description);
        workingCopy.setTimeZoneId(timeZoneId);
        messageLabel.setText("");
        return true;
    }

    private LocationDto copyOf(LocationDto source) {
        LocationDto dto = new LocationDto();
        if (source == null) {
            return dto;
        }

        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setDescription(source.getDescription());
        dto.setTimeZoneId(source.getTimeZoneId());
        return dto;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void loadTimeZones() {
        timeZoneHintLabel.setText("Loading time zones...");
        timeZoneService.getTimeZoneIds(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        applyTimeZones(List.of(DEFAULT_TIME_ZONE));
                        timeZoneHintLabel.setText("Failed to load time zones. Defaulted to UTC.");
                    }

                    @Override
                    public void onSuccess(List<String> result) {
                        applyTimeZones(result);
                        timeZoneHintLabel.setText("");
                    }
                }
        );
    }

    private void applyTimeZones(List<String> timeZones) {
        availableTimeZones = timeZones == null ? List.of() : timeZones;
        timeZoneRegionBox.clear();
        timeZoneCityBox.clear();

        List<String> regions = buildRegions(availableTimeZones);
        if (regions.isEmpty()) {
            regions = List.of(DEFAULT_TIME_ZONE);
        }

        for (String region : regions) {
            timeZoneRegionBox.addItem(region);
        }

        timeZoneRegionBox.setEnabled(true);
        timeZoneCityBox.setEnabled(true);
        saveButton.setEnabled(true);
        selectTimeZone(nullSafe(workingCopy.getTimeZoneId()));
    }

    private void selectTimeZone(String timeZoneId) {
        String effective = timeZoneId.isEmpty() ? DEFAULT_TIME_ZONE : timeZoneId;
        String region = resolveRegion(effective);
        String city = resolveCity(region, effective);

        selectRegion(region);
        populateCitiesForRegion(region, city);
    }

    private String getSelectedTimeZoneId() {
        int regionIndex = timeZoneRegionBox.getSelectedIndex();
        int cityIndex = timeZoneCityBox.getSelectedIndex();
        if (regionIndex < 0 || cityIndex < 0) {
            return "";
        }
        String region = nullSafe(timeZoneRegionBox.getItemText(regionIndex)).trim();
        String city = nullSafe(timeZoneCityBox.getItemText(cityIndex)).trim();
        if (region.isEmpty()) {
            return "";
        }
        if (city.isEmpty() || region.equals(city)) {
            return region;
        }
        return region + "/" + city;
    }

    private void bindTimeZoneSelection() {
        timeZoneRegionBox.addChangeHandler(event -> {
            String region = getSelectedRegion();
            populateCitiesForRegion(region, "");
        });
    }

    private List<String> buildRegions(List<String> timeZones) {
        List<String> regions = new java.util.ArrayList<>();
        for (String id : timeZones) {
            if (id == null || id.isBlank()) {
                continue;
            }
            String region = resolveRegion(id.trim());
            if (!regions.contains(region)) {
                regions.add(region);
            }
        }
        regions.sort(String::compareTo);
        return regions;
    }

    private void populateCitiesForRegion(String region, String preferredCity) {
        timeZoneCityBox.clear();
        if (region.isEmpty()) {
            timeZoneCityBox.addItem(DEFAULT_TIME_ZONE);
            timeZoneCityBox.setSelectedIndex(0);
            return;
        }

        List<String> cities = new java.util.ArrayList<>();
        for (String id : availableTimeZones) {
            if (id == null || id.isBlank()) {
                continue;
            }
            String trimmed = id.trim();
            if (resolveRegion(trimmed).equals(region)) {
                String city = resolveCity(region, trimmed);
                if (!cities.contains(city)) {
                    cities.add(city);
                }
            }
        }

        if (cities.isEmpty()) {
            cities.add(region);
        }

        cities.sort(String::compareTo);
        for (String city : cities) {
            timeZoneCityBox.addItem(city);
        }
        selectCity(preferredCity);
    }

    private void selectRegion(String region) {
        if (timeZoneRegionBox.getItemCount() == 0) {
            return;
        }
        for (int i = 0; i < timeZoneRegionBox.getItemCount(); i++) {
            if (region.equals(timeZoneRegionBox.getItemText(i))) {
                timeZoneRegionBox.setSelectedIndex(i);
                return;
            }
        }
        timeZoneRegionBox.setSelectedIndex(0);
    }

    private void selectCity(String preferredCity) {
        if (timeZoneCityBox.getItemCount() == 0) {
            return;
        }
        String effective = preferredCity == null || preferredCity.isEmpty()
                ? timeZoneCityBox.getItemText(0)
                : preferredCity;
        for (int i = 0; i < timeZoneCityBox.getItemCount(); i++) {
            if (effective.equals(timeZoneCityBox.getItemText(i))) {
                timeZoneCityBox.setSelectedIndex(i);
                return;
            }
        }
        timeZoneCityBox.setSelectedIndex(0);
    }

    private String getSelectedRegion() {
        int index = timeZoneRegionBox.getSelectedIndex();
        if (index < 0) {
            return "";
        }
        return nullSafe(timeZoneRegionBox.getItemText(index)).trim();
    }

    private String resolveRegion(String timeZoneId) {
        int slashIndex = timeZoneId.indexOf('/');
        if (slashIndex <= 0) {
            return timeZoneId;
        }
        return timeZoneId.substring(0, slashIndex);
    }

    private String resolveCity(String region, String timeZoneId) {
        if (region.isEmpty()) {
            return timeZoneId;
        }
        if (region.equals(timeZoneId)) {
            return region;
        }
        int prefixLength = region.length() + 1;
        if (timeZoneId.length() <= prefixLength) {
            return region;
        }
        return timeZoneId.substring(prefixLength);
    }
}
