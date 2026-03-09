package com.jc.halibut.uilib.forms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.jc.halibut.AuthSession;
import com.jc.halibut.LoginService;
import com.jc.halibut.LoginServiceAsync;
import com.jc.halibut.dto.LoginAccountDto;
import com.jc.halibut.uilib.grid.GridColumnDescriptor;
import com.jc.halibut.uilib.grid.ReusableGridWidget;

import java.util.Comparator;
import java.util.List;

public class ClientsFormPanel extends TopMenuFormPanel {
    private final LoginServiceAsync loginService = GWT.create(LoginService.class);
    private final AuthSession authSession = AuthSession.getInstance();

    private final ReusableGridWidget<LoginAccountDto> accountsGrid = new ReusableGridWidget<>();

    public ClientsFormPanel() {
        super("Clients");

        configureGrid();
        add(accountsGrid);
        loadAccounts();
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
                GridColumnDescriptor.of("Auto Restore", 120,
                        dto -> dto.isAutoSessionRestoreEnabled() ? "Enabled" : "Disabled"),
                GridColumnDescriptor.of("Active", 100,
                        dto -> dto.isActive() ? "Yes" : "No")
        );

        accountsGrid.setColumns(columns);
        accountsGrid.setRowTypeProvider(dto -> dto.getRole() == null ? "USER" : dto.getRole().name());
    }

    private void loadAccounts() {
        if (!authSession.hasSession()) {
            setStatusMessage("No active session.");
            return;
        }

        setStatusMessage("Loading accounts...");
        loginService.getLoginAccounts(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        setStatusMessage("Failed to load accounts.");
                    }

                    @Override
                    public void onSuccess(List<LoginAccountDto> result) {
                        accountsGrid.setRows(result == null ? List.of() : result);
                        if (result == null || result.isEmpty()) {
                            setStatusMessage("No accounts available for your role.");
                            return;
                        }
                        setStatusMessage("Loaded " + result.size() + " account(s).");
                    }
                }
        );
    }
}
