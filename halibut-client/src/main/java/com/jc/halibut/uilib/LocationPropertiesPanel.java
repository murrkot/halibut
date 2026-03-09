package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.jc.halibut.dto.LocationDto;

public class LocationPropertiesPanel extends DialogBox {
    private static final UserPropertiesPanelResources RESOURCES = GWT.create(UserPropertiesPanelResources.class);

    public interface SaveHandler {
        void onSave(LocationDto dto);
    }

    private final TextBox nameBox = new TextBox();
    private final TextArea descriptionBox = new TextArea();
    private final Label messageLabel = new Label();
    private final LocationDto workingCopy;

    public LocationPropertiesPanel(String title, LocationDto initialValue, SaveHandler saveHandler) {
        super(true, true);
        setText(title == null ? "Location Properties" : title);
        setAnimationEnabled(true);
        setGlassEnabled(true);

        RESOURCES.style().ensureInjected();
        setStyleName(RESOURCES.style().dialog());

        this.workingCopy = copyOf(initialValue);

        FlowPanel root = new FlowPanel();
        root.setStyleName(RESOURCES.style().root());

        root.add(new Label("Name"));
        nameBox.setStylePrimaryName(RESOURCES.style().input());
        nameBox.setText(nullSafe(workingCopy.getName()));
        root.add(nameBox);

        root.add(new Label("Description"));
        descriptionBox.setStylePrimaryName(RESOURCES.style().input());
        descriptionBox.setVisibleLines(4);
        descriptionBox.setCharacterWidth(40);
        descriptionBox.setText(nullSafe(workingCopy.getDescription()));
        root.add(descriptionBox);

        messageLabel.setStyleName(RESOURCES.style().message());
        root.add(messageLabel);

        FlowPanel actions = new FlowPanel();
        actions.setStyleName(RESOURCES.style().actions());

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setStylePrimaryName(RESOURCES.style().actionButton());
        cancelButton.setStylePrimaryName(RESOURCES.style().actionButton());

        saveButton.addClickHandler(event -> {
            if (!readForm()) {
                return;
            }
            hide();
            if (saveHandler != null) {
                saveHandler.onSave(copyOf(workingCopy));
            }
        });

        cancelButton.addClickHandler(event -> hide());

        actions.add(saveButton);
        actions.add(cancelButton);
        root.add(actions);

        setWidget(root);
    }

    private boolean readForm() {
        String name = trimToEmpty(nameBox.getText());
        String description = trimToEmpty(descriptionBox.getText());

        if (name.isEmpty()) {
            messageLabel.setText("Name is required.");
            return false;
        }
        if (description.isEmpty()) {
            messageLabel.setText("Description is required.");
            return false;
        }

        workingCopy.setName(name);
        workingCopy.setDescription(description);
        messageLabel.setText("");
        return true;
    }

    private LocationDto copyOf(LocationDto source) {
        LocationDto dto = new LocationDto();
        if (source == null) {
            return dto;
        }

        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setDescription(source.getDescription());
        return dto;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
