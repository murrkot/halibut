package com.jc.halibut.uilib;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.jc.halibut.CurrentLocation;

public class ProfilePanel extends FlowPanel {
    private final Label profileHeader = new Label();
    private final CheckBox autoRestoreCheckBox = new CheckBox("Auto restore session on app start");
    private final Button savePreferenceButton = new Button("Save preferences");
    private final Label statusLabel = new Label();

    private final TabPanel tabs = new TabPanel();
    private UsersGridPanel usersGridPanel;
    private LocationsGridPanel locationsGridPanel;
    private AuditLogGridPanel auditLogGridPanel;

    private final Button dashboardButton = new Button("Dashboard");
    private final Button logoutButton = new Button("Logout");

    public ProfilePanel() {
        setStyleName("halibut-profile-page");

        FlowPanel topArea = new FlowPanel();
        topArea.setStyleName("halibut-profile-top");
        topArea.add(new HTML("<h2>Profile</h2>"));
        profileHeader.setStyleName("halibut-profile-header");
        topArea.add(profileHeader);

        FlowPanel middleArea = new FlowPanel();
        middleArea.setStyleName("halibut-profile-middle");

        tabs.setStyleName("halibut-profile-tabs");
        tabs.addSelectionHandler(new SelectionHandler<>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                int index = event.getSelectedItem();
                if (index < 0 || index >= tabs.getWidgetCount()) {
                    return;
                }

                Widget selectedWidget = tabs.getWidget(index);
                if (selectedWidget == usersGridPanel && usersGridPanel != null) {
                    usersGridPanel.refreshDisplay();
                }
                if (selectedWidget == locationsGridPanel && locationsGridPanel != null) {
                    locationsGridPanel.refreshDisplay();
                }
                if (selectedWidget == auditLogGridPanel && auditLogGridPanel != null) {
                    auditLogGridPanel.refreshDisplay();
                }
            }
        });
        middleArea.add(tabs);

        FlowPanel bottomArea = new FlowPanel();
        bottomArea.setStyleName("halibut-profile-bottom");
        dashboardButton.addStyleName("button");
        logoutButton.addStyleName("button");
        bottomArea.add(dashboardButton);
        bottomArea.add(logoutButton);

        savePreferenceButton.addStyleName("button");
        statusLabel.setStyleName("halibut-profile-status");

        add(topArea);
        add(middleArea);
        add(bottomArea);
    }

    public void configureTabsForRole(String role) {
        tabs.clear();
        usersGridPanel = null;
        locationsGridPanel = null;
        auditLogGridPanel = null;

        String normalizedRole = role == null ? "USER" : role.trim().toUpperCase();
        switch (normalizedRole) {
            case "ADMIN":
                tabs.add(buildUsersTab(), "Users");
                tabs.add(buildLocationsTab(), "Locations");
                tabs.add(new HTML("<div class='halibut-profile-tab-content'>General account information.</div>"), "General");
                tabs.add(buildSecurityTab(true), "Security");
                break;
            case "MANAGER":
                tabs.add(new HTML("<div class='halibut-profile-tab-content'>General account information.</div>"), "General");
                tabs.add(buildSecurityTab(false), "Security");
                tabs.add(new HTML("<div class='halibut-profile-tab-content'>Team-level controls and management reports.</div>"), "Team");
                tabs.add(new HTML("<div class='halibut-profile-tab-content'>Manager operations and approvals.</div>"), "Operations");
                break;
            default:
                tabs.add(new HTML("<div class='halibut-profile-tab-content'>General account information.</div>"), "General");
                tabs.add(buildSecurityTab(false), "Security");
                tabs.add(new HTML("<div class='halibut-profile-tab-content'>Personal settings and preferences.</div>"), "Personal");
                break;
        }

        tabs.selectTab(0);
    }

    private Widget buildSecurityTab(boolean includeAuditLog) {
        if (includeAuditLog) {
            auditLogGridPanel = new AuditLogGridPanel();
            return auditLogGridPanel;
        }

        FlowPanel securityTab = new FlowPanel();
        securityTab.setStyleName("halibut-profile-tab-content halibut-profile-preferences");
        securityTab.add(autoRestoreCheckBox);
        securityTab.add(savePreferenceButton);
        securityTab.add(statusLabel);
        return securityTab;
    }

    private UsersGridPanel buildUsersTab() {
        usersGridPanel = new UsersGridPanel();
        return usersGridPanel;
    }

    private LocationsGridPanel buildLocationsTab() {
        locationsGridPanel = new LocationsGridPanel();
        return locationsGridPanel;
    }

    public void setUserInfo(String displayName, String role) {
        String safeName = displayName == null || displayName.trim().isEmpty() ? "Unknown user" : displayName;
        String safeRole = role == null || role.trim().isEmpty() ? "USER" : role;

        String currentLocationName = CurrentLocation.getInstance().getCurrentName();
        if (currentLocationName == null || currentLocationName.trim().isEmpty()) {
            profileHeader.setText("User: " + safeName + " | Role: " + safeRole);
            return;
        }

        profileHeader.setText("User: " + safeName + " | Role: " + safeRole
                + " { Location : " + currentLocationName + " }");
    }

    public Button getDashboardButton() {
        return dashboardButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public CheckBox getAutoRestoreCheckBox() {
        return autoRestoreCheckBox;
    }

    public Button getSavePreferenceButton() {
        return savePreferenceButton;
    }

    public void setStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }
}

