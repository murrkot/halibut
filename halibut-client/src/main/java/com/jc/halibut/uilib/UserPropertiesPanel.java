package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.jc.halibut.dto.LoginAccountDto;
import com.jc.halibut.dto.LoginAccountRole;

public class UserPropertiesPanel extends DialogBox {
    private static final UserPropertiesPanelResources RESOURCES = GWT.create(UserPropertiesPanelResources.class);

    public interface SaveHandler {
        void onSave(LoginAccountDto dto);
    }

    private final TextBox usernameBox = new TextBox();
    private final TextBox displayNameBox = new TextBox();
    private final ListBox roleBox = new ListBox();
    private final CheckBox autoRestoreBox = new CheckBox("Auto restore session");
    private final CheckBox activeBox = new CheckBox("Active");
    private final Label messageLabel = new Label();

    private final LoginAccountDto workingCopy;

    public UserPropertiesPanel(String title, LoginAccountDto initialValue, SaveHandler saveHandler) {
        super(true, true);
        setText(title == null ? "User Properties" : title);
        setAnimationEnabled(true);
        setGlassEnabled(true);

        RESOURCES.style().ensureInjected();
        setStyleName(RESOURCES.style().dialog());

        this.workingCopy = copyOf(initialValue);

        FlowPanel root = new FlowPanel();
        root.setStyleName(RESOURCES.style().root());

        root.add(new Label("Username"));
        usernameBox.setStylePrimaryName(RESOURCES.style().input());
        usernameBox.setText(nullSafe(workingCopy.getUsername()));
        root.add(usernameBox);

        root.add(new Label("Display Name"));
        displayNameBox.setStylePrimaryName(RESOURCES.style().input());
        displayNameBox.setText(nullSafe(workingCopy.getDisplayName()));
        root.add(displayNameBox);

        root.add(new Label("Role"));
        roleBox.setStylePrimaryName(RESOURCES.style().input());
        for (LoginAccountRole role : LoginAccountRole.values()) {
            roleBox.addItem(role.name());
        }
        roleBox.setSelectedIndex(indexOfRole(workingCopy.getRole()));
        root.add(roleBox);

        autoRestoreBox.setValue(workingCopy.isAutoSessionRestoreEnabled());
        activeBox.setValue(workingCopy.isActive());
        root.add(autoRestoreBox);
        root.add(activeBox);

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
        String username = trimToEmpty(usernameBox.getText());
        String displayName = trimToEmpty(displayNameBox.getText());

        if (username.isEmpty()) {
            messageLabel.setText("Username is required.");
            return false;
        }

        if (displayName.isEmpty()) {
            messageLabel.setText("Display Name is required.");
            return false;
        }

        workingCopy.setUsername(username);
        workingCopy.setDisplayName(displayName);

        int roleIndex = roleBox.getSelectedIndex();
        String roleValue = roleIndex >= 0 ? roleBox.getValue(roleIndex) : LoginAccountRole.USER.name();
        workingCopy.setRole(LoginAccountRole.valueOf(roleValue));

        workingCopy.setAutoSessionRestoreEnabled(Boolean.TRUE.equals(autoRestoreBox.getValue()));
        workingCopy.setActive(Boolean.TRUE.equals(activeBox.getValue()));

        messageLabel.setText("");
        return true;
    }

    private int indexOfRole(LoginAccountRole role) {
        LoginAccountRole effective = role == null ? LoginAccountRole.USER : role;
        for (int i = 0; i < roleBox.getItemCount(); i++) {
            if (effective.name().equals(roleBox.getValue(i))) {
                return i;
            }
        }
        return 0;
    }

    private LoginAccountDto copyOf(LoginAccountDto source) {
        LoginAccountDto dto = new LoginAccountDto();
        if (source == null) {
            dto.setRole(LoginAccountRole.USER);
            dto.setActive(true);
            dto.setAutoSessionRestoreEnabled(false);
            return dto;
        }

        dto.setId(source.getId());
        dto.setUsername(source.getUsername());
        dto.setDisplayName(source.getDisplayName());
        dto.setRole(source.getRole());
        dto.setAutoSessionRestoreEnabled(source.isAutoSessionRestoreEnabled());
        dto.setActive(source.isActive());
        return dto;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
