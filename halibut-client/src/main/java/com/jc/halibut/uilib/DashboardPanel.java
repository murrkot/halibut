package com.jc.halibut.uilib;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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

    private final FlowPanel mainContent = new FlowPanel();

    public DashboardPanel() {
        setStyleName("halibut-dashboard");

        FlowPanel topArea = new FlowPanel();
        topArea.setStyleName("halibut-dashboard-top");

        FlowPanel topMenu = new FlowPanel();
        topMenu.setStyleName("halibut-dashboard-menu");
        topMenu.add(homeButton);
        topMenu.add(ordersButton);
        topMenu.add(analyticsButton);
        topMenu.add(inventoryButton);
        topMenu.add(clientsButton);
        topMenu.add(settingsButton);
        topMenu.add(helpButton);
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
}
