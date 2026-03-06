package com.jc.halibut.presenter;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;
import com.jc.halibut.uilib.DashboardPanel;

public class DashboardPresenter implements Presenter {
    private final DashboardPanel view;

    public DashboardPresenter(DashboardPanel view) {
        this.view = view;
        bind();
    }

    private void bind() {
        view.getOpenProfileButton().addClickHandler(event -> History.newItem("profile"));
        view.getLogoutButton().addClickHandler(event -> History.newItem("login"));
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(view);
    }
}
