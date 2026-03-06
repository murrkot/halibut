package com.jc.halibut.uilib;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class DashboardPanel extends FlowPanel {
    private final Button reportsButton = new Button("Reports");
    private final Button openProfileButton = new Button("Profile");
    private final Button logoutButton = new Button("Logout");

    public DashboardPanel() {
        setStyleName("halibut-dashboard");

        FlowPanel topArea = new FlowPanel();
        topArea.setStyleName("halibut-dashboard-top");

        FlowPanel topMenu = new FlowPanel();
        topMenu.setStyleName("halibut-dashboard-menu");
        topMenu.add(new Button("Home"));
        topMenu.add(new Button("Orders"));
        topMenu.add(new Button("Analytics"));
        topMenu.add(new Button("Inventory"));
        topMenu.add(new Button("Clients"));
        topMenu.add(new Button("Settings"));
        topMenu.add(new Button("Help"));
        topArea.add(topMenu);

        FlowPanel centerArea = new FlowPanel();
        centerArea.setStyleName("halibut-dashboard-center");

        FlowPanel leftColumn = new FlowPanel();
        leftColumn.setStyleName("halibut-dashboard-col halibut-dashboard-col-left");
        leftColumn.add(new HTML("<h3>Left</h3>"));

        FlowPanel middleColumn = new FlowPanel();
        middleColumn.setStyleName("halibut-dashboard-col halibut-dashboard-col-main");
        middleColumn.add(new HTML("<h3>Main</h3>"));

        FlowPanel rightColumn = new FlowPanel();
        rightColumn.setStyleName("halibut-dashboard-col halibut-dashboard-col-right");
        rightColumn.add(new HTML("<h3>Right</h3>"));

        centerArea.add(leftColumn);
        centerArea.add(middleColumn);
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
}
