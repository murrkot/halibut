package com.jc.halibut.uilib.grid;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.List;

public class ReusableGridWidget<T> extends Composite {
    private static final int DEFAULT_VIEWPORT_HEIGHT_PX = 320;
    private static final int ESTIMATED_ROW_HEIGHT_PX = 28;

    private static final HalibutDataGridResources GRID_RESOURCES = GWT.create(HalibutDataGridResources.class);

    private final FlowPanel root = new FlowPanel();
    private final FocusPanel focusPanel = new FocusPanel();
    private final DataGrid<T> grid = new DataGrid<>(50, GRID_RESOURCES);
    private final HalibutDataGridResources.HalibutDataGridStyle gridStyle = GRID_RESOURCES.dataGridStyle();

    private final ListDataProvider<T> dataProvider = new ListDataProvider<>();
    private final ColumnSortEvent.ListHandler<T> sortHandler;
    private final SingleSelectionModel<T> selectionModel = new SingleSelectionModel<>();

    public ReusableGridWidget() {
        root.setWidth("100%");

        focusPanel.getElement().setTabIndex(0);
        focusPanel.setWidth("100%");

        gridStyle.ensureInjected();

        grid.setWidth("100%");
        grid.setHeight(DEFAULT_VIEWPORT_HEIGHT_PX + "px");

        grid.setSelectionModel(
                selectionModel,
                DefaultSelectionEventManager.createDefaultManager()
        );

        dataProvider.addDataDisplay(grid);
        sortHandler = new ColumnSortEvent.ListHandler<>(dataProvider.getList());
        grid.addColumnSortHandler(sortHandler);

        focusPanel.add(grid);
        root.add(focusPanel);

        bindNavigation();

        initWidget(root);
        setWidth("100%");
    }
    public void setAdaptiveHeight(boolean adaptiveHeight) {
        if (adaptiveHeight) {
            setHeight("100%");
            root.setHeight("100%");
            focusPanel.setHeight("100%");
            grid.setHeight("100%");
            return;
        }

        setHeight("");
        root.setHeight("");
        focusPanel.setHeight("");
        grid.setHeight(DEFAULT_VIEWPORT_HEIGHT_PX + "px");
    }

    public void setViewportHeightPx(int heightPx) {
        int safeHeightPx = Math.max(1, heightPx);
        String height = safeHeightPx + "px";
        setHeight(height);
        root.setHeight(height);
        focusPanel.setHeight(height);
        grid.setHeight(height);
    }
    public void setColumns(List<GridColumnDescriptor<T>> columns) {
        while (grid.getColumnCount() > 0) {
            grid.removeColumn(0);
        }

        for (GridColumnDescriptor<T> descriptor : columns) {
            Column<T, String> column = new Column<>(new TextCell()) {
                @Override
                public String getValue(T item) {
                    if (item == null) {
                        return "";
                    }
                    String value = descriptor.getValueProvider().getValue(item);
                    return value == null ? "" : value;
                }
            };

            grid.addColumn(column, descriptor.getHeader());
            if (descriptor.getWidthPx() > 0) {
                grid.setColumnWidth(column, descriptor.getWidthPx(), Style.Unit.PX);
            }

            if (descriptor.isSortable() && descriptor.getComparator() != null) {
                column.setSortable(true);
                sortHandler.setComparator(column, descriptor.getComparator());
            }
        }

        grid.redraw();
    }

    public void setRows(List<T> rows) {
        List<T> target = dataProvider.getList();
        target.clear();
        if (rows != null) {
            target.addAll(rows);
        }

        int rowCount = target.size();
        grid.setPageSize(Math.max(1, rowCount));
        grid.setVisibleRange(0, Math.max(1, rowCount));

        dataProvider.refresh();
        Scheduler.get().scheduleDeferred(() -> {
            List<T> currentRows = dataProvider.getList();
            if (!currentRows.isEmpty()) {
                selectRowAt(0);
            } else {
                selectionModel.clear();
            }
            grid.redraw();
        });
    }

    public T getSelectedRow() {
        return selectionModel.getSelectedObject();
    }

    public void refreshDisplay() {
        Scheduler.get().scheduleDeferred(() -> {
            dataProvider.refresh();
            grid.redraw();
        });
    }

    public void setRowTypeProvider(GridRowTypeProvider<T> rowTypeProvider) {
        if (rowTypeProvider == null) {
            grid.setRowStyles(null);
            grid.redraw();
            return;
        }

        grid.setRowStyles((item, rowIndex) -> {
            String rowType = rowTypeProvider.getRowType(item);
            if (rowType == null) {
                return "";
            }

            switch (rowType.trim().toUpperCase()) {
                case "ADMIN":
                    return gridStyle.halibutGridRowAdmin();
                case "MANAGER":
                    return gridStyle.halibutGridRowManager();
                case "USER":
                    return gridStyle.halibutGridRowUser();
                default:
                    return "";
            }
        });

        grid.redraw();
    }

    private void bindNavigation() {
        focusPanel.addKeyDownHandler(this::handleKeyDown);
        focusPanel.addMouseWheelHandler(this::handleMouseWheel);
        focusPanel.addClickHandler(event -> focusPanel.setFocus(true));
    }

    private void handleKeyDown(KeyDownEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        int keyCode = nativeEvent.getKeyCode();

        switch (keyCode) {
            case KeyCodes.KEY_DOWN:
                moveSelectionBy(1);
                consume(event);
                break;
            case KeyCodes.KEY_UP:
                moveSelectionBy(-1);
                consume(event);
                break;
            case KeyCodes.KEY_PAGEDOWN:
                moveSelectionBy(getPageJumpSize());
                consume(event);
                break;
            case KeyCodes.KEY_PAGEUP:
                moveSelectionBy(-getPageJumpSize());
                consume(event);
                break;
            case KeyCodes.KEY_HOME:
                selectFirstRow();
                consume(event);
                break;
            case KeyCodes.KEY_END:
                selectLastRow();
                consume(event);
                break;
            default:
                break;
        }
    }

    private void handleMouseWheel(MouseWheelEvent event) {
        int delta = event.getDeltaY();
        if (delta == 0) {
            return;
        }

        moveSelectionBy(delta > 0 ? 1 : -1);
    }

    private void moveSelectionBy(int delta) {
        List<T> rows = dataProvider.getList();
        if (rows.isEmpty()) {
            return;
        }

        T selected = selectionModel.getSelectedObject();
        int currentIndex = selected == null ? 0 : rows.indexOf(selected);
        if (currentIndex < 0) {
            currentIndex = 0;
        }

        int nextIndex = currentIndex + delta;
        if (nextIndex < 0) {
            nextIndex = 0;
        }
        if (nextIndex >= rows.size()) {
            nextIndex = rows.size() - 1;
        }

        selectRowAt(nextIndex);
    }

    private void selectFirstRow() {
        selectRowAt(0);
    }

    private void selectLastRow() {
        List<T> rows = dataProvider.getList();
        if (rows.isEmpty()) {
            return;
        }
        selectRowAt(rows.size() - 1);
    }

    private int getPageJumpSize() {
        int viewportHeight = grid.getOffsetHeight();
        if (viewportHeight <= 0) {
            viewportHeight = DEFAULT_VIEWPORT_HEIGHT_PX;
        }
        return Math.max(1, viewportHeight / ESTIMATED_ROW_HEIGHT_PX);
    }

    private void selectRowAt(int index) {
        List<T> rows = dataProvider.getList();
        if (rows.isEmpty()) {
            return;
        }

        int safeIndex = Math.max(0, Math.min(index, rows.size() - 1));
        T selectedRow = rows.get(safeIndex);
        selectionModel.setSelected(selectedRow, true);
        grid.setKeyboardSelectedRow(safeIndex);
        grid.redraw();
    }

    private void consume(KeyDownEvent event) {
        event.preventDefault();
        event.stopPropagation();
    }
}




