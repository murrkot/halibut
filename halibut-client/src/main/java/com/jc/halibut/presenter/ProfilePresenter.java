package com.jc.halibut.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.jc.halibut.AuthSession;
import com.jc.halibut.LoginService;
import com.jc.halibut.LoginServiceAsync;
import com.jc.halibut.uilib.ProfilePanel;

public class ProfilePresenter implements Presenter {
    private final ProfilePanel view;
    private final LoginServiceAsync loginService = GWT.create(LoginService.class);
    private final AuthSession authSession = AuthSession.getInstance();

    public ProfilePresenter(ProfilePanel view) {
        this.view = view;
        bind();
    }

    private void bind() {
        view.setUserInfo(authSession.getDisplayName(), authSession.getRole());
        view.configureTabsForRole(authSession.getRole());
        view.getAutoRestoreCheckBox().setValue(authSession.isAutoSessionRestoreEnabled());

        view.getSavePreferenceButton().addClickHandler(event -> savePreference());
        view.getDashboardButton().addClickHandler(event -> History.newItem("dashboard"));
        view.getLogoutButton().addClickHandler(event -> logoutCurrentSession());
    }

    private void savePreference() {
        if (!authSession.hasSession()) {
            view.setStatus("No active session.");
            return;
        }

        boolean enabled = Boolean.TRUE.equals(view.getAutoRestoreCheckBox().getValue());
        view.getSavePreferenceButton().setEnabled(false);
        view.setStatus("Saving...");

        loginService.updateAutoSessionRestorePreference(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                enabled,
                new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        view.getSavePreferenceButton().setEnabled(true);
                        view.setStatus("Failed to save preference.");
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        view.getSavePreferenceButton().setEnabled(true);
                        if (Boolean.TRUE.equals(result)) {
                            authSession.updateAutoSessionRestoreEnabled(enabled);
                            view.setStatus("Preference saved.");
                            return;
                        }
                        view.setStatus("Preference was not saved.");
                    }
                }
        );
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
