package com.jc.halibut.uilib;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.jc.halibut.AuthSession;
import com.jc.halibut.CurrentLocation;
import com.jc.halibut.dto.LocationDto;
import com.jc.halibut.uilib.forms.AnalyticsFormPanel;
import com.jc.halibut.uilib.forms.ClientsFormPanel;
import com.jc.halibut.uilib.forms.HelpFormPanel;
import com.jc.halibut.uilib.forms.HomeFormPanel;
import com.jc.halibut.uilib.forms.InventoryFormPanel;
import com.jc.halibut.uilib.forms.OrdersFormPanel;
import com.jc.halibut.uilib.forms.SettingsFormPanel;
import com.jc.halibut.uilib.forms.TopMenuFormPanel;

public class DashboardPanel extends FlowPanel {
    private final Button homeButton = new Button("Income");
    private final Button ordersButton = new Button("Orders");
    private final Button analyticsButton = new Button("Analytics");
    private final Button inventoryButton = new Button("Inventory");
    private final Button clientsButton = new Button("Clients");
    private final Button settingsButton = new Button("Settings");
    private final Button helpButton = new Button("Help");

    private final Button reportsButton = new Button("Reports");
    private final Button openProfileButton = new Button("Profile");
    private final Button logoutButton = new Button("Logout");

    private final Label statusLabel = new Label();
    private final AuthSession authSession = AuthSession.getInstance();
    private final CurrentLocation currentLocation = CurrentLocation.getInstance();
    private final CurrentLocation.Listener locationListener = location -> updateStatus();
    private Timer clockTimer;

    private final FlowPanel mainContent = new FlowPanel();

    public DashboardPanel() {
        setStyleName("halibut-dashboard");

        FlowPanel topArea = new FlowPanel();
        topArea.setStyleName("halibut-dashboard-top");

        FlowPanel statusBar = new FlowPanel();
        statusBar.setStyleName("halibut-dashboard-status");
        statusBar.add(statusLabel);

        FlowPanel topMenu = new FlowPanel();
        topMenu.setStyleName("halibut-dashboard-menu");
        topMenu.add(homeButton);
        topMenu.add(ordersButton);
        topMenu.add(analyticsButton);
        topMenu.add(inventoryButton);
        topMenu.add(clientsButton);
        topMenu.add(settingsButton);
        topMenu.add(helpButton);
        topArea.add(statusBar);
        topArea.add(topMenu);

        FlowPanel centerArea = new FlowPanel();
        centerArea.setStyleName("halibut-dashboard-center");

        FlowPanel leftColumn = new FlowPanel();
        leftColumn.setStyleName("halibut-dashboard-col halibut-dashboard-col-left");
        leftColumn.add(new HTML("<h3>Left</h3>"));

        mainContent.setStyleName("halibut-dashboard-col halibut-dashboard-col-main");
        showMainPlaceholder();

        FlowPanel rightColumn = new FlowPanel();
        rightColumn.setStyleName("halibut-dashboard-col halibut-dashboard-col-right");
        rightColumn.add(new HTML("<h3>Right</h3>"));

        centerArea.add(leftColumn);
        centerArea.add(mainContent);
        centerArea.add(rightColumn);

        FlowPanel bottomArea = new FlowPanel();
        bottomArea.setStyleName("halibut-dashboard-bottom");
        bottomArea.add(new HTML("<h3>Bottom</h3>"));

        FlowPanel bottomMenu = new FlowPanel();
        bottomMenu.setStyleName("halibut-dashboard-bottom-menu");
        bottomMenu.add(reportsButton);
        bottomMenu.add(openProfileButton);
        bottomMenu.add(logoutButton);
        bottomArea.add(bottomMenu);

        add(topArea);
        add(centerArea);
        add(bottomArea);

        bindTopMenuHandlers();
        setDashboardButtonsEnabled(true);
        currentLocation.addListener(locationListener);
        updateStatus();
    }

    private void bindTopMenuHandlers() {
        homeButton.addClickHandler(event -> openHomeForm());
        ordersButton.addClickHandler(event -> openOrdersForm());
        analyticsButton.addClickHandler(event -> openAnalyticsForm());
        inventoryButton.addClickHandler(event -> openInventoryForm());
        clientsButton.addClickHandler(event -> openClientsForm());
        settingsButton.addClickHandler(event -> openSettingsForm());
        helpButton.addClickHandler(event -> openHelpForm());
    }

    private void openHomeForm() {
        showForm(new HomeFormPanel());
    }

    private void openOrdersForm() {
        showForm(new OrdersFormPanel());
    }

    private void openAnalyticsForm() {
        showForm(new AnalyticsFormPanel());
    }

    private void openInventoryForm() {
        showForm(new InventoryFormPanel());
    }

    private void openClientsForm() {
        showForm(new ClientsFormPanel());
    }

    private void openSettingsForm() {
        showForm(new SettingsFormPanel());
    }

    private void openHelpForm() {
        showForm(new HelpFormPanel());
    }

    private void showMainPlaceholder() {
        mainContent.clear();
        mainContent.add(new HTML("<h3>Main</h3>"));
        setDashboardButtonsEnabled(true);
    }

    private void showForm(TopMenuFormPanel panel) {
        setDashboardButtonsEnabled(false);

        panel.getCancelButton().addClickHandler(event -> showMainPlaceholder());
        panel.getOkButton().addClickHandler(event -> { panel.setStatusMessage(panel.getFormName() + " saved."); showMainPlaceholder(); });

        mainContent.clear();
        mainContent.add(panel);
    }

    private void setDashboardButtonsEnabled(boolean enabled) {
        homeButton.setEnabled(enabled);
        ordersButton.setEnabled(enabled);
        analyticsButton.setEnabled(enabled);
        inventoryButton.setEnabled(enabled);
        clientsButton.setEnabled(enabled);
        settingsButton.setEnabled(enabled);
        helpButton.setEnabled(enabled);

        reportsButton.setEnabled(enabled);
        openProfileButton.setEnabled(enabled);
        logoutButton.setEnabled(enabled);
    }

    public Button getReportsButton() {
        return reportsButton;
    }

    public Button getOpenProfileButton() {
        return openProfileButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (clockTimer == null) {
            clockTimer = new Timer() {
                @Override
                public void run() {
                    updateStatus();
                }
            };
        }
        clockTimer.scheduleRepeating(1000);
    }

    @Override
    protected void onUnload() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
        currentLocation.removeListener(locationListener);
        super.onUnload();
    }

    private void updateStatus() {
        String user = nullSafe(authSession.getDisplayName(), "Unknown");
        LocationDto location = currentLocation.getCurrent();
        String locationName = location == null ? "Not set" : nullSafe(location.getName(), "Not set");
        String timeZoneId = location == null ? "" : nullSafe(location.getTimeZoneId(), "");
        if (timeZoneId.isEmpty()) {
            timeZoneId = "UTC";
        }
        String time = formatLocalTime(timeZoneId);
        statusLabel.setText("User: " + user + " | Location: " + locationName + " | Local Time: " + time);
    }

    private String nullSafe(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String formatLocalTime(String timeZoneId) {
        return formatLocalTimeNative(timeZoneId);
    }

    private static native String formatLocalTimeNative(String timeZoneId) /*-{
        try {
            var options = { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false };
            if (timeZoneId && timeZoneId.length) {
                options.timeZone = timeZoneId;
            }
            return new Intl.DateTimeFormat('en-GB', options).format(new Date());
        } catch (e) {
            try {
                return new Date().toLocaleTimeString();
            } catch (ignore) {
                return '';
            }
        }
    }-*/;
}
