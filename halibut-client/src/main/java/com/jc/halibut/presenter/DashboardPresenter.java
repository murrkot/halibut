package com.jc.halibut.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.jc.halibut.AuthSession;
import com.jc.halibut.LoginService;
import com.jc.halibut.LoginServiceAsync;
import com.jc.halibut.uilib.DashboardPanel;

public class DashboardPresenter implements Presenter {
    private final DashboardPanel view;
    private final LoginServiceAsync loginService = GWT.create(LoginService.class);
    private final AuthSession authSession = AuthSession.getInstance();

    public DashboardPresenter(DashboardPanel view) {
        this.view = view;
        bind();
    }

    private void bind() {
        view.getOpenProfileButton().addClickHandler(event -> History.newItem("profile"));
        view.getLogoutButton().addClickHandler(event -> logoutCurrentSession());
    }

    private void logoutCurrentSession() {
        if (!authSession.hasSession()) {
            History.newItem("login");
            return;
        }

        loginService.deactivateSession(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        authSession.clear();
                        History.newItem("login");
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        authSession.clear();
                        History.newItem("login");
                    }
                }
        );
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(view);
    }
}
