package org.dooq;

import org.dooq.api.Table;

public class KeyBuilder {

    private final Table<?, ?> table;
    private Object partitionValue;
    private Object sortValue;

    public KeyBuilder(Table<?, ?> table) {
        this.table = table;
    }

    public KeyBuilder partition(Object partitionValue) {
        this.partitionValue = partitionValue;
        return this;
    }

    public KeyBuilder sort(Object sortValue) {
        this.sortValue = sortValue;
        return this;
    }

    public Key build() {
        return Key.of(table.getPartitionColumn(), partitionValue, table.getSortColumn(), sortValue);
    }
}
