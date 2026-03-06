package com.jc.halibut.presenter;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;
import com.jc.halibut.uilib.ProfilePanel;

public class ProfilePresenter implements Presenter {
    private final ProfilePanel view;

    public ProfilePresenter(ProfilePanel view) {
        this.view = view;
        bind();
    }

    private void bind() {
        view.getBackButton().addClickHandler(event -> History.newItem("dashboard"));
        view.getLogoutButton().addClickHandler(event -> History.newItem("login"));
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(view);
    }
}
