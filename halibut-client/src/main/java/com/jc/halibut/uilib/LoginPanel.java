package com.jc.halibut.uilib;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.jc.halibut.presenter.LoginPresenter;

public class LoginPanel extends FlowPanel implements LoginPresenter.Display {
    private final HTML title;
    private final TextBox userNameField = new TextBox();
    private final PasswordTextBox passwordField = new PasswordTextBox();
    private final Button loginButton = new Button("Login");
    private final Label errorLabel = new Label();

    public LoginPanel(String title) {
        setStyleName("halibut-login-container");

        this.title = new HTML("<h2>" + title + "</h2>");

        Label userLabel = new Label("Username:");
        userNameField.getElement().setAttribute("placeholder", "Enter username");

        Label passLabel = new Label("Password:");
        passwordField.getElement().setAttribute("placeholder", "Enter password");

        loginButton.addStyleName("button");
        errorLabel.setStyleName("halibut-login-error");

        if (!title.isEmpty()) {
            add(this.title);
        }
        add(userLabel);
        add(userNameField);
        add(passLabel);
        add(passwordField);
        add(loginButton);
        add(errorLabel);
    }

    @Override
    public HasValue<String> getUserName() {
        return userNameField;
    }

    @Override
    public HasValue<String> getUserPassword() {
        return passwordField;
    }

    @Override
    public Button getLoginButton() {
        return loginButton;
    }

    @Override
    public void setLoginButtonEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
    }

    @Override
    public void setErrorMessage(String message) {
        errorLabel.setText(message == null ? "" : message);
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
