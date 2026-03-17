package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.jc.halibut.AuthSession;
import com.jc.halibut.LoginService;
import com.jc.halibut.LoginServiceAsync;
import com.jc.halibut.dto.LoginAccountDto;
import com.jc.halibut.dto.LoginAccountRole;
import com.jc.halibut.uilib.grid.GridColumnDescriptor;
import com.jc.halibut.uilib.grid.HalibuteGridHorizontalMenuResources;
import com.jc.halibut.uilib.grid.ReusableGridWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UsersGridPanel extends FlowPanel {
    private static final HalibuteGridHorizontalMenuResources MENU_RESOURCES =
            GWT.create(HalibuteGridHorizontalMenuResources.class);

    private final LoginServiceAsync loginService = GWT.create(LoginService.class);
    private final AuthSession authSession = AuthSession.getInstance();

    private final FlowPanel menuBar = new FlowPanel();
    private final Button addButton = new Button("Add");
    private final Button editButton = new Button("Edit");
    private final Button removeButton = new Button("Remove");

    private final Label loadedInfoLabel = new Label();
    private final Label statusLabel = new Label();
    private final ReusableGridWidget<LoginAccountDto> accountsGrid = new ReusableGridWidget<>();
    private final List<LoginAccountDto> currentRows = new ArrayList<>();

    private HandlerRegistration shortcutRegistration;
    private HandlerRegistration windowResizeRegistration;

    public UsersGridPanel() {
        setStyleName("halibut-profile-tab-content");
        addStyleName("halibut-users-tab");
        accountsGrid.addStyleName("halibut-users-grid");
//        accountsGrid.setStyleName("halibut-users-grid");

        loadedInfoLabel.setStyleName("halibut-grid-info");
        statusLabel.setStyleName("halibut-profile-status");

        MENU_RESOURCES.style().ensureInjected();

        configureMenu();
        configureGrid();
        bindShortcuts();

        add(menuBar);
        add(accountsGrid);
        add(loadedInfoLabel);
        add(statusLabel);

        loadAccounts();
    }

    private void configureMenu() {
        String menuBarStyle = MENU_RESOURCES.style().menuBar();
        String menuButtonStyle = MENU_RESOURCES.style().menuButton();

        menuBar.setStyleName(menuBarStyle);

        addButton.setStylePrimaryName(menuButtonStyle);
        editButton.setStylePrimaryName(menuButtonStyle);
        removeButton.setStylePrimaryName(menuButtonStyle);

        addButton.setTitle("Add user (Shift+F4)");
        editButton.setTitle("Edit selected user (F4)");
        removeButton.setTitle("Remove selected user (F8)");

        addButton.addClickHandler(event -> addRow());
        editButton.addClickHandler(event -> editSelectedRow());
        removeButton.addClickHandler(event -> removeSelectedRow());

        menuBar.add(addButton);
        menuBar.add(editButton);
        menuBar.add(removeButton);
    }

    private void configureGrid() {
        List<GridColumnDescriptor<LoginAccountDto>> columns = List.of(
                GridColumnDescriptor.sortable("Id", 80,
                        dto -> dto.getId() == null ? "" : String.valueOf(dto.getId()),
                        Comparator.comparing(LoginAccountDto::getId, Comparator.nullsLast(Long::compareTo))),
                GridColumnDescriptor.sortable("Username", 140,
                        LoginAccountDto::getUsername,
                        Comparator.comparing(LoginAccountDto::getUsername, Comparator.nullsLast(String::compareTo))),
                GridColumnDescriptor.sortable("Display Name", 180,
                        LoginAccountDto::getDisplayName,
                        Comparator.comparing(LoginAccountDto::getDisplayName, Comparator.nullsLast(String::compareTo))),
                GridColumnDescriptor.sortable("Role", 120,
                        dto -> dto.getRole() == null ? "" : dto.getRole().name(),
                        Comparator.comparing(dto -> dto.getRole() == null ? "" : dto.getRole().name())),
                GridColumnDescriptor.of("Session Timeout", 140,
                        dto -> dto.getSessionTimeout() == null ? "" : dto.getSessionTimeout()),
                GridColumnDescriptor.of("Auto Restore", 120,
                        dto -> dto.isAutoSessionRestoreEnabled() ? "Enabled" : "Disabled"),
                GridColumnDescriptor.of("Active", 100,
                        dto -> dto.isActive() ? "Yes" : "No")
        );

        accountsGrid.setAdaptiveHeight(false);
        accountsGrid.setColumns(columns);
        accountsGrid.setRowTypeProvider(dto -> dto.getRole() == null ? "USER" : dto.getRole().name());
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (windowResizeRegistration == null) {
            windowResizeRegistration = Window.addResizeHandler(event -> updateGridHeightToMiddleArea());
        }
        updateGridHeightToMiddleArea();
    }

    public void updateGridHeightToMiddleArea() {
        Scheduler.get().scheduleDeferred(() -> {
            int availableHeight = resolveHeightFromProfileMiddleArea();
            if (availableHeight <= 0) {
                availableHeight = getOffsetHeight();
            }
            if (availableHeight <= 0) {
                return;
            }

            int menuHeight = menuBar.getOffsetHeight();
            int loadedInfoHeight = loadedInfoLabel.getOffsetHeight();
            int statusHeight = statusLabel.getOffsetHeight();
            int verticalSpacing = 12;

            int gridHeight = availableHeight - menuHeight - loadedInfoHeight - statusHeight - verticalSpacing;
            accountsGrid.setViewportHeightPx(Math.max(180, gridHeight));
        });
    }

    private int resolveHeightFromProfileMiddleArea() {
        Element middleArea = findAncestorByClassName(getElement(), "halibut-profile-middle");
        if (middleArea == null) {
            return -1;
        }

        int middleHeight = middleArea.getOffsetHeight();
        if (middleHeight <= 0) {
            return -1;
        }

        Element tabBar = findDescendantByClassName(middleArea, "gwt-TabBar");
        int tabBarHeight = tabBar == null ? 0 : tabBar.getOffsetHeight();

        int tabChrome = 18;
        return middleHeight - tabBarHeight - tabChrome;
    }

    private Element findAncestorByClassName(Element from, String className) {
        Element current = from;
        while (current != null) {
            if (current.hasClassName(className)) {
                return current;
            }
            current = current.getParentElement();
        }
        return null;
    }

    private Element findDescendantByClassName(Element root, String className) {
        if (root == null) {
            return null;
        }

        Element child = root.getFirstChildElement();
        while (child != null) {
            if (child.hasClassName(className)) {
                return child;
            }
            Element nested = findDescendantByClassName(child, className);
            if (nested != null) {
                return nested;
            }
            child = child.getNextSiblingElement();
        }
        return null;
    }

    private void bindShortcuts() {
        shortcutRegistration = Event.addNativePreviewHandler(event -> {
            if (!isAttached() || !isVisible() || event.getTypeInt() != Event.ONKEYDOWN) {
                return;
            }

            int keyCode = event.getNativeEvent().getKeyCode();
            boolean shift = event.getNativeEvent().getShiftKey();

            if (keyCode == KeyCodes.KEY_F4 && shift) {
                addRow();
                event.cancel();
                return;
            }

            if (keyCode == KeyCodes.KEY_F4) {
                editSelectedRow();
                event.cancel();
                return;
            }

            if (keyCode == KeyCodes.KEY_F8) {
                removeSelectedRow();
                event.cancel();
            }
        });
    }

    private void loadAccounts() {
        if (!authSession.hasSession()) {
            setStatus("No active session.");
            return;
        }

        setButtonsEnabled(false);
        setStatus("Loading accounts...");
        loginService.getLoginAccounts(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        setButtonsEnabled(true);
                        setStatus("Failed to load accounts.");
                    }

                    @Override
                    public void onSuccess(List<LoginAccountDto> result) {
                        setButtonsEnabled(true);
                        currentRows.clear();
                        if (result != null) {
                            currentRows.addAll(result);
                        }

                        accountsGrid.setRows(currentRows);
                        setLoadedInfo("Loaded " + currentRows.size() + " record(s).");
                        updateGridHeightToMiddleArea();
                        setStatus("");
                    }
                }
        );
    }

    private void addRow() {
        long nextIndex = currentRows.size() + 1L;

        LoginAccountDto draft = new LoginAccountDto();
        draft.setUsername("new_user_" + nextIndex);
        draft.setDisplayName("New User " + nextIndex);
        draft.setRole(LoginAccountRole.USER);
        draft.setAutoSessionRestoreEnabled(false);
        draft.setActive(true);

        UserPropertiesPanel dialog = new UserPropertiesPanel("Add User", draft, saved -> {
            saveAccount(saved, "User added.");
        });

        dialog.center();
        dialog.show();
    }

    private void editSelectedRow() {
        LoginAccountDto selected = accountsGrid.getSelectedRow();
        if (selected == null) {
            setStatus("Select a row to edit.");
            return;
        }

        LoginAccountDto draft = copyOf(selected);
        UserPropertiesPanel dialog = new UserPropertiesPanel("Edit User", draft, saved -> {
            saveAccount(saved, "User updated.");
        });

        dialog.center();
        dialog.show();
    }

    private void removeSelectedRow() {
        LoginAccountDto selected = accountsGrid.getSelectedRow();
        if (selected == null) {
            setStatus("Select a row to remove.");
            return;
        }

        DeleteUserConfirmationPanel dialog = new DeleteUserConfirmationPanel(selected.getUsername(), () -> {
            setButtonsEnabled(false);
            setStatus("Deleting user...");
            loginService.deleteLoginAccount(
                    authSession.getUserId(),
                    authSession.getSessionId(),
                    authSession.getSecurityToken(),
                    selected.getId(),
                    new AsyncCallback<>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            setButtonsEnabled(true);
                            setStatus("Failed to delete user.");
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            if (!Boolean.TRUE.equals(result)) {
                                setButtonsEnabled(true);
                                setStatus("User was not deleted.");
                                return;
                            }
                            setStatus("User deleted. Reloading...");
                            loadAccounts();
                        }
                    }
            );
        });

        dialog.center();
        dialog.show();
    }

    private void saveAccount(LoginAccountDto account, String successMessage) {
        if (!authSession.hasSession()) {
            setStatus("No active session.");
            return;
        }

        setButtonsEnabled(false);
        setStatus("Saving user...");
        loginService.saveLoginAccount(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                account,
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        setButtonsEnabled(true);
                        setStatus("Failed to save user.");
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (!Boolean.TRUE.equals(result)) {
                            setButtonsEnabled(true);
                            setStatus("User was not saved.");
                            return;
                        }
                        setStatus(successMessage + " Reloading...");
                        loadAccounts();
                    }
                }
        );
    }

    private LoginAccountDto copyOf(LoginAccountDto source) {
        LoginAccountDto dto = new LoginAccountDto();
        if (source == null) {
            return dto;
        }

        dto.setId(source.getId());
        dto.setUsername(source.getUsername());
        dto.setDisplayName(source.getDisplayName());
        dto.setRole(source.getRole());
        dto.setAutoSessionRestoreEnabled(source.isAutoSessionRestoreEnabled());
        dto.setActive(source.isActive());
        dto.setSessionTimeout(source.getSessionTimeout());
        return dto;
    }

    public void refreshDisplay() {
        accountsGrid.refreshDisplay();
        updateGridHeightToMiddleArea();
    }

    private void setButtonsEnabled(boolean enabled) {
        addButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
    }

    private void setLoadedInfo(String message) {
        loadedInfoLabel.setText(message == null ? "" : message);
    }

    private void setStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }

    @Override
    protected void onUnload() {
        if (shortcutRegistration != null) {
            shortcutRegistration.removeHandler();
            shortcutRegistration = null;
        }
        if (windowResizeRegistration != null) {
            windowResizeRegistration.removeHandler();
            windowResizeRegistration = null;
        }
        super.onUnload();
    }
}





