package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.jc.halibut.AuthSession;
import com.jc.halibut.CurrentLocation;
import com.jc.halibut.location.LocationService;
import com.jc.halibut.location.LocationServiceAsync;
import com.jc.halibut.dto.LocationDto;
import com.jc.halibut.uilib.grid.GridColumnDescriptor;
import com.jc.halibut.uilib.grid.HalibuteGridHorizontalMenuResources;
import com.jc.halibut.uilib.grid.ReusableGridWidget;

import java.util.ArrayList;
import java.util.Date;
import java.util.Comparator;
import java.util.List;

public class LocationsGridPanel extends FlowPanel {
    private static final HalibuteGridHorizontalMenuResources MENU_RESOURCES =
            GWT.create(HalibuteGridHorizontalMenuResources.class);

    private final LocationServiceAsync locationService = GWT.create(LocationService.class);
    private final AuthSession authSession = AuthSession.getInstance();

    private final FlowPanel menuBar = new FlowPanel();
    private final Button addButton = new Button("Add");
    private final Button editButton = new Button("Edit");
    private final Button removeButton = new Button("Remove");
    private final Button setButton = new Button("Set");

    private final Label loadedInfoLabel = new Label();
    private final Label statusLabel = new Label();
    private final ReusableGridWidget<LocationDto> locationsGrid = new ReusableGridWidget<>();
    private final List<LocationDto> currentRows = new ArrayList<>();

    private HandlerRegistration shortcutRegistration;
    private HandlerRegistration windowResizeRegistration;

    public LocationsGridPanel() {
        setStyleName("halibut-profile-tab-content");
        addStyleName("halibut-users-tab");
        addStyleName("halibut-locations-tab");
        locationsGrid.addStyleName("halibut-users-grid");
        locationsGrid.addStyleName("halibut-locations-grid");

        loadedInfoLabel.setStyleName("halibut-grid-info");
        statusLabel.setStyleName("halibut-profile-status");

        MENU_RESOURCES.style().ensureInjected();

        configureMenu();
        configureGrid();
        bindShortcuts();

        add(menuBar);
        add(locationsGrid);
        add(loadedInfoLabel);
        add(statusLabel);

        loadLocations();
    }

    private void configureMenu() {
        String menuBarStyle = MENU_RESOURCES.style().menuBar();
        String menuButtonStyle = MENU_RESOURCES.style().menuButton();

        menuBar.setStyleName(menuBarStyle);

        addButton.setStylePrimaryName(menuButtonStyle);
        editButton.setStylePrimaryName(menuButtonStyle);
        removeButton.setStylePrimaryName(menuButtonStyle);
        setButton.setStylePrimaryName(menuButtonStyle);

        addButton.setTitle("Add location (Shift+F4)");
        editButton.setTitle("Edit selected location (F4)");
        removeButton.setTitle("Remove selected location (F8)");
        setButton.setTitle("Set selected location");

        addButton.addClickHandler(event -> addRow());
        editButton.addClickHandler(event -> editSelectedRow());
        removeButton.addClickHandler(event -> removeSelectedRow());
        setButton.addClickHandler(event -> setCurrentLocation());

        menuBar.add(addButton);
        menuBar.add(editButton);
        menuBar.add(removeButton);
        menuBar.add(setButton);
    }

    private void configureGrid() {
        List<GridColumnDescriptor<LocationDto>> columns = List.of(
                GridColumnDescriptor.sortable("Id", 90,
                        dto -> dto.getId() == null ? "" : String.valueOf(dto.getId()),
                        Comparator.comparing(LocationDto::getId, Comparator.nullsLast(Long::compareTo))),
                GridColumnDescriptor.sortable("Name", 220,
                        LocationDto::getName,
                        Comparator.comparing(LocationDto::getName, Comparator.nullsLast(String::compareTo))),
                GridColumnDescriptor.of("Time Zone", 200,
                        dto -> dto.getTimeZoneId() == null ? "" : dto.getTimeZoneId()),
                GridColumnDescriptor.of("Description", 520,
                        LocationDto::getDescription)
        );

        locationsGrid.setAdaptiveHeight(false);
        locationsGrid.setColumns(columns);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (windowResizeRegistration == null) {
            windowResizeRegistration = Window.addResizeHandler(event -> updateGridHeightToMiddleArea());
        }
        updateGridHeightToMiddleArea();
    }

    public void updateGridHeightToMiddleArea() {
        Scheduler.get().scheduleDeferred(() -> {
            int availableHeight = resolveHeightFromProfileMiddleArea();
            if (availableHeight <= 0) {
                availableHeight = getOffsetHeight();
            }
            if (availableHeight <= 0) {
                return;
            }

            int menuHeight = menuBar.getOffsetHeight();
            int loadedInfoHeight = loadedInfoLabel.getOffsetHeight();
            int statusHeight = statusLabel.getOffsetHeight();
            int verticalSpacing = 12;

            int gridHeight = availableHeight - menuHeight - loadedInfoHeight - statusHeight - verticalSpacing;
            locationsGrid.setViewportHeightPx(Math.max(180, gridHeight));
        });
    }

    private int resolveHeightFromProfileMiddleArea() {
        Element middleArea = findAncestorByClassName(getElement(), "halibut-profile-middle");
        if (middleArea == null) {
            return -1;
        }

        int middleHeight = middleArea.getOffsetHeight();
        if (middleHeight <= 0) {
            return -1;
        }

        Element tabBar = findDescendantByClassName(middleArea, "gwt-TabBar");
        int tabBarHeight = tabBar == null ? 0 : tabBar.getOffsetHeight();

        int tabChrome = 18;
        return middleHeight - tabBarHeight - tabChrome;
    }

    private Element findAncestorByClassName(Element from, String className) {
        Element current = from;
        while (current != null) {
            if (current.hasClassName(className)) {
                return current;
            }
            current = current.getParentElement();
        }
        return null;
    }

    private Element findDescendantByClassName(Element root, String className) {
        if (root == null) {
            return null;
        }

        Element child = root.getFirstChildElement();
        while (child != null) {
            if (child.hasClassName(className)) {
                return child;
            }
            Element nested = findDescendantByClassName(child, className);
            if (nested != null) {
                return nested;
            }
            child = child.getNextSiblingElement();
        }
        return null;
    }

    private void bindShortcuts() {
        shortcutRegistration = Event.addNativePreviewHandler(event -> {
            if (!isAttached() || !isVisible() || event.getTypeInt() != Event.ONKEYDOWN) {
                return;
            }

            int keyCode = event.getNativeEvent().getKeyCode();
            boolean shift = event.getNativeEvent().getShiftKey();

            if (keyCode == KeyCodes.KEY_F4 && shift) {
                addRow();
                event.cancel();
                return;
            }

            if (keyCode == KeyCodes.KEY_F4) {
                editSelectedRow();
                event.cancel();
                return;
            }

            if (keyCode == KeyCodes.KEY_F8) {
                removeSelectedRow();
                event.cancel();
            }
        });
    }

    private void loadLocations() {
        if (!authSession.hasSession()) {
            setStatus("No active session.");
            return;
        }

        setButtonsEnabled(false);
        setStatus("Loading locations...");
        locationService.getLocations(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        setButtonsEnabled(true);
                        setStatus("Failed to load locations.");
                    }

                    @Override
                    public void onSuccess(List<LocationDto> result) {
                        setButtonsEnabled(true);
                        currentRows.clear();
                        if (result != null) {
                            currentRows.addAll(result);
                        }

                        locationsGrid.setRows(currentRows);
                        setLoadedInfo("Loaded " + currentRows.size() + " record(s).");
                        updateGridHeightToMiddleArea();
                        setStatus("");
                    }
                }
        );
    }

    private void addRow() {
        LocationDto draft = new LocationDto();
        draft.setName("New Location");
        draft.setDescription("Description");

        LocationPropertiesPanel dialog = new LocationPropertiesPanel("Add Location", draft, saved -> {
            saveLocation(saved, "Location added.");
        });

        dialog.center();
        dialog.show();
    }

    private void editSelectedRow() {
        LocationDto selected = locationsGrid.getSelectedRow();
        if (selected == null) {
            setStatus("Select a row to edit.");
            return;
        }

        LocationDto draft = copyOf(selected);
        LocationPropertiesPanel dialog = new LocationPropertiesPanel("Edit Location", draft, saved -> {
            saveLocation(saved, "Location updated.");
        });

        dialog.center();
        dialog.show();
    }

    private void removeSelectedRow() {
        LocationDto selected = locationsGrid.getSelectedRow();
        if (selected == null) {
            setStatus("Select a row to remove.");
            return;
        }

        DeleteLocationConfirmationPanel dialog = new DeleteLocationConfirmationPanel(selected.getName(), () -> {
            setButtonsEnabled(false);
            setStatus("Deleting location...");
            locationService.deleteLocation(
                    authSession.getUserId(),
                    authSession.getSessionId(),
                    authSession.getSecurityToken(),
                    selected.getId(),
                    new AsyncCallback<>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            setButtonsEnabled(true);
                            setStatus("Failed to delete location.");
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            if (!Boolean.TRUE.equals(result)) {
                                setButtonsEnabled(true);
                                setStatus("Location was not deleted.");
                                return;
                            }
                            setStatus("Location deleted. Reloading...");
                            loadLocations();
                        }
                    }
            );
        });

        dialog.center();
        dialog.show();
    }
    private void setCurrentLocation() {
        LocationDto selected = locationsGrid.getSelectedRow();
        if (selected == null) {
            setStatus("Select a row to set location.");
            return;
        }

        if (selected.getId() == null || selected.getName() == null || selected.getName().trim().isEmpty()) {
            setStatus("Selected location has invalid data.");
            return;
        }

        if (!authSession.hasSession()) {
            setStatus("No active session.");
            return;
        }

        SetLocationConfirmationPanel dialog = new SetLocationConfirmationPanel(selected.getName(),
                () -> applyCurrentLocation(selected));
        dialog.center();
        dialog.show();
    }

    private void applyCurrentLocation(LocationDto selected) {
        Date expires = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 20);

        locationService.getLocationById(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                selected.getId(),
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        persistLocationCookie(selected, expires);
                        CurrentLocation.getInstance().setCurrent(selected);
                        setStatus("Location is set: " + selected.getName());
                    }

                    @Override
                    public void onSuccess(LocationDto result) {
                        if (result == null) {
                            persistLocationCookie(selected, expires);
                            CurrentLocation.getInstance().setCurrent(selected);
                            setStatus("Location is set: " + selected.getName());
                            return;
                        }

                        persistLocationCookie(result, expires);
                        CurrentLocation.getInstance().setCurrent(result);
                        setStatus("Location is set: " + result.getName());
                    }
                }
        );
    }

    private void persistLocationCookie(LocationDto location, Date expires) {
        if (location == null || location.getId() == null || location.getName() == null) {
            return;
        }
        String cookiePath = "/";
        Cookies.setCookie(CurrentLocation.COOKIE_LOCATION_ID, String.valueOf(location.getId()), expires, cookiePath, null, false);
        Cookies.setCookie(CurrentLocation.COOKIE_LOCATION_NAME, URL.encodeQueryString(location.getName().trim()), expires, cookiePath, null, false);
    }
    private void saveLocation(LocationDto location, String successMessage) {
        if (!authSession.hasSession()) {
            setStatus("No active session.");
            return;
        }

        setButtonsEnabled(false);
        setStatus("Saving location...");
        locationService.saveLocation(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                location,
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        setButtonsEnabled(true);
                        setStatus("Failed to save location.");
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (!Boolean.TRUE.equals(result)) {
                            setButtonsEnabled(true);
                            setStatus("Location was not saved.");
                            return;
                        }
                        setStatus(successMessage + " Reloading...");
                        loadLocations();
                    }
                }
        );
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

    public void refreshDisplay() {
        locationsGrid.refreshDisplay();
        updateGridHeightToMiddleArea();
    }

    private void setButtonsEnabled(boolean enabled) {
        addButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        setButton.setEnabled(enabled);
    }

    private void setLoadedInfo(String message) {
        loadedInfoLabel.setText(message == null ? "" : message);
    }

    private void setStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }

    @Override
    protected void onUnload() {
        if (shortcutRegistration != null) {
            shortcutRegistration.removeHandler();
            shortcutRegistration = null;
        }
        if (windowResizeRegistration != null) {
            windowResizeRegistration.removeHandler();
            windowResizeRegistration = null;
        }
        super.onUnload();
    }
}








