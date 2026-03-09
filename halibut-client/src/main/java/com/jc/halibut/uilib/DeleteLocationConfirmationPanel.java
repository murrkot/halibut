package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class DeleteLocationConfirmationPanel extends DialogBox {
    public interface ConfirmHandler {
        void onConfirm();
    }

    private static final UserPropertiesPanelResources RESOURCES = GWT.create(UserPropertiesPanelResources.class);

    public DeleteLocationConfirmationPanel(String locationName, ConfirmHandler confirmHandler) {
        super(true, true);
        setText("Confirm Delete");
        setAnimationEnabled(true);
        setGlassEnabled(true);

        RESOURCES.style().ensureInjected();
        setStyleName(RESOURCES.style().dialog());

        FlowPanel root = new FlowPanel();
        root.setStyleName(RESOURCES.style().root());

        Label message = new Label("Delete location '" + safe(locationName) + "'? This action cannot be undone.");
        root.add(message);

        FlowPanel actions = new FlowPanel();
        actions.setStyleName(RESOURCES.style().actions());

        Button confirmButton = new Button("Delete");
        Button cancelButton = new Button("Cancel");

        confirmButton.setStylePrimaryName(RESOURCES.style().actionButton());
        cancelButton.setStylePrimaryName(RESOURCES.style().actionButton());

        confirmButton.addClickHandler(event -> {
            hide();
            if (confirmHandler != null) {
                confirmHandler.onConfirm();
            }
        });

        cancelButton.addClickHandler(event -> hide());

        actions.add(confirmButton);
        actions.add(cancelButton);
        root.add(actions);

        setWidget(root);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "(unknown)" : value;
    }
}
