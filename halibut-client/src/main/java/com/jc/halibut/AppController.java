package com.jc.halibut;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.jc.halibut.event.LoginEvent;
import com.jc.halibut.event.LoginEventHandler;
import com.jc.halibut.presenter.DashboardPresenter;
import com.jc.halibut.presenter.LoginPresenter;
import com.jc.halibut.presenter.Presenter;
import com.jc.halibut.presenter.ProfilePresenter;
import com.jc.halibut.uilib.DashboardPanel;
import com.jc.halibut.uilib.LoginPanel;
import com.jc.halibut.uilib.ProfilePanel;

public class AppController implements Presenter, ValueChangeHandler<String>, LoginEventHandler {
    private static final String TOKEN_LOGIN = "login";
    private static final String TOKEN_DASHBOARD = "dashboard";
    private static final String TOKEN_PROFILE = "profile";

    private final HandlerManager eventBus;
    private final LoginServiceAsync loginService = GWT.create(LoginService.class);
    private final AuthSession authSession = AuthSession.getInstance();
    private HasWidgets container;

    public AppController(HandlerManager eventBus) {
        this.eventBus = eventBus;
        authSession.loadFromStorageIfEnabled();
        bind();
    }

    private void bind() {
        History.addValueChangeHandler(this);
        eventBus.addHandler(LoginEvent.TYPE, this);
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        showPage(event.getValue());
    }

    @Override
    public void onLogin(LoginEvent event) {
        History.newItem(TOKEN_DASHBOARD);
    }

    @Override
    public void go(HasWidgets container) {
        this.container = container;

        if (History.getToken() == null || History.getToken().trim().isEmpty()) {
            if (authSession.isAutoSessionRestoreEnabled() && authSession.hasSession()) {
                ensureAuthenticatedThen(() -> History.newItem(TOKEN_DASHBOARD));
                return;
            }

            History.newItem(TOKEN_LOGIN);
            return;
        }

        History.fireCurrentHistoryState();
    }

    private void showPage(String token) {
        String safeToken = token == null ? "" : token.toLowerCase();

        switch (safeToken) {
            case TOKEN_DASHBOARD:
                ensureAuthenticatedThen(() -> renderPresenter(new DashboardPresenter(new DashboardPanel())));
                break;
            case TOKEN_PROFILE:
                ensureAuthenticatedThen(() -> renderPresenter(new ProfilePresenter(new ProfilePanel())));
                break;
            case TOKEN_LOGIN:
            default:
                if (!TOKEN_LOGIN.equals(safeToken)) {
                    History.newItem(TOKEN_LOGIN, false);
                }
                renderPresenter(new LoginPresenter(eventBus, new LoginPanel("Halibut Login")));
                break;
        }
    }

    private void ensureAuthenticatedThen(Runnable onValidSession) {
        if (!authSession.hasSession()) {
            History.newItem(TOKEN_LOGIN, false);
            renderPresenter(new LoginPresenter(eventBus, new LoginPanel("Halibut Login")));
            return;
        }

        loginService.validateSession(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        authSession.clear();
                        History.newItem(TOKEN_LOGIN, false);
                        renderPresenter(new LoginPresenter(eventBus, new LoginPanel("Halibut Login")));
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (Boolean.TRUE.equals(result)) {
                            onValidSession.run();
                            return;
                        }

                        authSession.clear();
                        History.newItem(TOKEN_LOGIN, false);
                        renderPresenter(new LoginPresenter(eventBus, new LoginPanel("Halibut Login")));
                    }
                }
        );
    }

    private void renderPresenter(Presenter presenter) {
        presenter.go(container);
    }
}
