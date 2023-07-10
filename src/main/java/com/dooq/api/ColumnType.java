package com.dooq.api;

public enum ColumnType {
    PARTITION, SORT, NORMAL;

    public boolean isKey() {
        return this == PARTITION || this == SORT;
    }
}
