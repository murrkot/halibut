package com.jc.halibut.uilib;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class ProfilePanel extends FlowPanel {
    private final Button backButton = new Button("Back to dashboard");
    private final Button logoutButton = new Button("Logout");

    public ProfilePanel() {
        setStyleName("halibut-page");

        add(new HTML("<h2>Profile</h2>"));
        add(new HTML("<p>This is a placeholder profile page for MVP navigation.</p>"));

        backButton.addStyleName("button");
        logoutButton.addStyleName("button");

        add(backButton);
        add(logoutButton);
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }
}
