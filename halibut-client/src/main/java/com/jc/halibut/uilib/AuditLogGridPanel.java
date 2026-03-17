package com.jc.halibut.uilib;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.jc.halibut.AuthSession;
import com.jc.halibut.audit.AuditService;
import com.jc.halibut.audit.AuditServiceAsync;
import com.jc.halibut.dto.AuditEventDto;
import com.jc.halibut.uilib.grid.GridColumnDescriptor;
import com.jc.halibut.uilib.grid.ReusableGridWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AuditLogGridPanel extends FlowPanel {
    private static final int DEFAULT_LIMIT = 0;

    private final AuditServiceAsync auditService = GWT.create(AuditService.class);
    private final AuthSession authSession = AuthSession.getInstance();

    private final ReusableGridWidget<AuditEventDto> auditGrid = new ReusableGridWidget<>();
    private final Label loadedInfoLabel = new Label();
    private final Label statusLabel = new Label();
    private final List<AuditEventDto> currentRows = new ArrayList<>();

    private HandlerRegistration windowResizeRegistration;

    public AuditLogGridPanel() {
        setStyleName("halibut-profile-tab-content");
        addStyleName("halibut-audit-tab");
        addStyleName("halibut-users-tab");

        auditGrid.addStyleName("halibut-users-grid");
        auditGrid.addStyleName("halibut-audit-grid");
        loadedInfoLabel.setStyleName("halibut-grid-info");
        statusLabel.setStyleName("halibut-profile-status");

        configureGrid();

        FlowPanel menuBar = new FlowPanel();
        menuBar.setStyleName("menuBar");
        add(menuBar);
        add(auditGrid);
        add(loadedInfoLabel);
        add(statusLabel);

        loadAuditEvents();
    }

    private void configureGrid() {
        DateTimeFormat formatter = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
        List<GridColumnDescriptor<AuditEventDto>> columns = List.of(
                GridColumnDescriptor.sortable("Time", 180,
                        dto -> formatEventTime(dto, formatter),
                        Comparator.comparingLong(AuditEventDto::getEventTime)),
                GridColumnDescriptor.sortable("Type", 140,
                        AuditEventDto::getEventType,
                        Comparator.comparing(AuditEventDto::getEventType, Comparator.nullsLast(String::compareTo))),
                GridColumnDescriptor.sortable("User", 160,
                        dto -> dto.getUserName() == null ? "" : dto.getUserName(),
                        Comparator.comparing(dto -> dto.getUserName() == null ? "" : dto.getUserName())),
                GridColumnDescriptor.of("Location", 200,
                        dto -> dto.getLocationName() == null ? "" : dto.getLocationName()),
                GridColumnDescriptor.of("IP", 140,
                        dto -> dto.getRemoteAddress() == null ? "" : dto.getRemoteAddress()),
                GridColumnDescriptor.of("Success", 90,
                        dto -> dto.isSuccess() ? "Yes" : "No"),
                GridColumnDescriptor.of("Details", 320,
                        dto -> dto.getDetails() == null ? "" : dto.getDetails())
        );

        auditGrid.setAdaptiveHeight(false);
        auditGrid.setColumns(columns);
        auditGrid.setViewportHeightPx(260);
    }

    private String formatEventTime(AuditEventDto dto, DateTimeFormat formatter) {
        if (dto == null) {
            return "";
        }
        long ts = dto.getEventTime();
        if (ts <= 0) {
            return "";
        }
        return formatter.format(new Date(ts));
    }


    private void loadAuditEvents() {
        if (!authSession.hasSession()) {
            setStatus("No active session.");
            return;
        }

        setStatus("Loading audit events...");
        auditService.getAuditEvents(
                authSession.getUserId(),
                authSession.getSessionId(),
                authSession.getSecurityToken(),
                DEFAULT_LIMIT,
                new AsyncCallback<>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        setStatus("Failed to load audit events.");
                    }

                    @Override
                    public void onSuccess(List<AuditEventDto> result) {
                        currentRows.clear();
                        if (result != null) {
                            currentRows.addAll(result);
                        }
                        currentRows.sort(Comparator.comparingLong(AuditEventDto::getEventTime).reversed());
                        auditGrid.setRows(currentRows);
                        setLoadedInfo("Loaded " + currentRows.size() + " record(s).");
                        updateGridHeightToMiddleArea();
                        setStatus("");
                    }
                }
        );
    }

    public void refreshDisplay() {
        auditGrid.refreshDisplay();
        updateGridHeightToMiddleArea();
        loadAuditEvents();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (windowResizeRegistration == null) {
            windowResizeRegistration = Window.addResizeHandler(event -> updateGridHeightToMiddleArea());
        }
        updateGridHeightToMiddleArea();
    }

    @Override
    protected void onUnload() {
        if (windowResizeRegistration != null) {
            windowResizeRegistration.removeHandler();
            windowResizeRegistration = null;
        }
        super.onUnload();
    }

    private void updateGridHeightToMiddleArea() {
        Scheduler.get().scheduleDeferred(() -> {
            int availableHeight = resolveHeightFromProfileMiddleArea();
            if (availableHeight <= 0) {
                availableHeight = getOffsetHeight();
            }
            if (availableHeight <= 0) {
                return;
            }

            int menuHeight = resolveMenuBarHeight();
            int loadedInfoHeight = loadedInfoLabel.getOffsetHeight();
            int statusHeight = statusLabel.getOffsetHeight();
            int verticalSpacing = 12;

            int gridHeight = availableHeight - menuHeight - loadedInfoHeight - statusHeight - verticalSpacing;
            auditGrid.setViewportHeightPx(Math.max(180, gridHeight));
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

    private int resolveMenuBarHeight() {
        Element menuBar = findDescendantByClassName(getElement(), "menuBar");
        if (menuBar == null) {
            return 0;
        }
        return Math.max(0, menuBar.getOffsetHeight());
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

    private void setLoadedInfo(String message) {
        loadedInfoLabel.setText(message == null ? "" : message);
    }

    private void setStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }
}
