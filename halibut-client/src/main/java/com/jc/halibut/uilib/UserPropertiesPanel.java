package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
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
    private final TextBox sessionTimeoutBox = new TextBox();
    private final Label sessionTimeoutHintLabel = new Label();
    private final PasswordTextBox passwordBox = new PasswordTextBox();
    private final PasswordTextBox confirmPasswordBox = new PasswordTextBox();
    private final Label passwordHintLabel = new Label();
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

        root.add(new Label("Session Timeout"));
        sessionTimeoutBox.setStylePrimaryName(RESOURCES.style().input());
        sessionTimeoutBox.getElement().setAttribute("placeholder", "e.g. 30m or 1h");
        sessionTimeoutBox.setText(nullSafe(workingCopy.getSessionTimeout()));
        root.add(sessionTimeoutBox);

        sessionTimeoutHintLabel.setStyleName(RESOURCES.style().hint());
        sessionTimeoutHintLabel.setText("Allowed: 1-59m or 1h (60m).");
        root.add(sessionTimeoutHintLabel);

        root.add(new Label("Password"));
        passwordBox.setStylePrimaryName(RESOURCES.style().input());
        root.add(passwordBox);

        root.add(new Label("Confirm Password"));
        confirmPasswordBox.setStylePrimaryName(RESOURCES.style().input());
        root.add(confirmPasswordBox);

        boolean isNewAccount = workingCopy.getId() == null;
        passwordHintLabel.setStyleName(RESOURCES.style().hint());
        passwordHintLabel.setText(isNewAccount
                ? "Password is required for new users."
                : "Leave password blank to keep the current one.");
        root.add(passwordHintLabel);

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
        String sessionTimeout = trimToEmpty(sessionTimeoutBox.getText());
        String password = trimToEmpty(passwordBox.getText());
        String confirmPassword = trimToEmpty(confirmPasswordBox.getText());

        if (username.isEmpty()) {
            messageLabel.setText("Username is required.");
            return false;
        }

        if (displayName.isEmpty()) {
            messageLabel.setText("Display Name is required.");
            return false;
        }

        if (!sessionTimeout.isEmpty() && !isValidSessionTimeout(sessionTimeout)) {
            messageLabel.setText("Session Timeout must be 1-59m or 1h.");
            return false;
        }

        boolean isNewAccount = workingCopy.getId() == null;
        if (isNewAccount && password.isEmpty()) {
            messageLabel.setText("Password is required for new users.");
            return false;
        }

        if (!password.isEmpty() || !confirmPassword.isEmpty()) {
            if (password.length() < 3) {
                messageLabel.setText("Password must be at least 3 characters.");
                return false;
            }
            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match.");
                return false;
            }
            workingCopy.setPlainPassword(password);
        } else {
            workingCopy.setPlainPassword(null);
        }

        workingCopy.setUsername(username);
        workingCopy.setDisplayName(displayName);
        workingCopy.setSessionTimeout(normalizeSessionTimeout(sessionTimeout));

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
        dto.setPlainPassword(source.getPlainPassword());
        dto.setSessionTimeout(source.getSessionTimeout());
        return dto;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private boolean isValidSessionTimeout(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        String trimmed = value.trim().toLowerCase();
        if ("1h".equals(trimmed) || "60m".equals(trimmed)) {
            return true;
        }
        if (!trimmed.endsWith("m")) {
            return false;
        }
        String minutesPart = trimmed.substring(0, trimmed.length() - 1);
        try {
            int minutes = Integer.parseInt(minutesPart);
            return minutes >= 1 && minutes <= 59;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String normalizeSessionTimeout(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "30m";
        }
        String trimmed = value.trim().toLowerCase();
        if ("60m".equals(trimmed)) {
            return "1h";
        }
        return trimmed;
    }
}
