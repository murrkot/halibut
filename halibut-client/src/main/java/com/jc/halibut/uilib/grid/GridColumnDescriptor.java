package com.jc.halibut.uilib.grid;

import java.util.Comparator;

public final class GridColumnDescriptor<T> {
    private final String header;
    private final int widthPx;
    private final boolean sortable;
    private final GridValueProvider<T> valueProvider;
    private final Comparator<T> comparator;

    private GridColumnDescriptor(String header,
                                 int widthPx,
                                 boolean sortable,
                                 GridValueProvider<T> valueProvider,
                                 Comparator<T> comparator) {
        this.header = header;
        this.widthPx = widthPx;
        this.sortable = sortable;
        this.valueProvider = valueProvider;
        this.comparator = comparator;
    }

    public static <T> GridColumnDescriptor<T> of(String header,
                                                 int widthPx,
                                                 GridValueProvider<T> valueProvider) {
        return new GridColumnDescriptor<>(header, widthPx, false, valueProvider, null);
    }

    public static <T> GridColumnDescriptor<T> sortable(String header,
                                                       int widthPx,
                                                       GridValueProvider<T> valueProvider,
                                                       Comparator<T> comparator) {
        return new GridColumnDescriptor<>(header, widthPx, true, valueProvider, comparator);
    }

    public String getHeader() {
        return header;
    }

    public int getWidthPx() {
        return widthPx;
    }

    public boolean isSortable() {
        return sortable;
    }

    public GridValueProvider<T> getValueProvider() {
        return valueProvider;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }
}
