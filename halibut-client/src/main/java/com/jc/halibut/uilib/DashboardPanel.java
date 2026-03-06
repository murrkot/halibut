package com.jc.halibut.uilib;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class DashboardPanel extends FlowPanel {
    private final Button openProfileButton = new Button("Open profile");
    private final Button logoutButton = new Button("Logout");

    public DashboardPanel() {
        setStyleName("halibut-page");

        add(new HTML("<h2>Dashboard</h2>"));
        add(new HTML("<p>Welcome to Halibut MVP dashboard.</p>"));

        openProfileButton.addStyleName("button");
        logoutButton.addStyleName("button");

        add(openProfileButton);
        add(logoutButton);
    }

    public Button getOpenProfileButton() {
        return openProfileButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }
}
