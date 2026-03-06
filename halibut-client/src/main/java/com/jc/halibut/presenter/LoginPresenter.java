package com.jc.halibut.presenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.jc.halibut.event.LoginEvent;

public class LoginPresenter implements Presenter {
    public interface Display {
        HasValue<String> getUserName();
        HasValue<String> getUserPassword();
        HasClickHandlers getLoginButton();
        void setErrorMessage(String message);
        Widget asWidget();
    }

    private final HandlerManager eventBus;
    private final Display display;

    public LoginPresenter(HandlerManager eventBus, Display display) {
        this.eventBus = eventBus;
        this.display = display;
        bind();
    }

    private void bind() {
        display.getLoginButton().addClickHandler(event -> attemptLogin());
    }

    private void attemptLogin() {
        String username = valueOrEmpty(display.getUserName().getValue()).trim();
        String password = valueOrEmpty(display.getUserPassword().getValue()).trim();

        if (username.length() < 3) {
            display.setErrorMessage("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 3) {
            display.setErrorMessage("Password must be at least 3 characters.");
            return;
        }

        display.setErrorMessage("");
        eventBus.fireEvent(new LoginEvent());
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(display.asWidget());
    }
}
