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

        //Not all tables have a sort key
        if (table.getSortColumn() == null) {
            return new Key()
                    .setPartitionKey(table.getPartitionColumn(), partitionValue);
        }

        return Key.of(table.getPartitionColumn(), partitionValue, table.getSortColumn(), sortValue);
    }
}
