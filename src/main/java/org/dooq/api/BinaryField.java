package org.dooq.api;

import org.dooq.Key;

public class BinaryField<T, R extends DynamoRecord<R>, K extends Key> extends Field<T, R, K> {
    public BinaryField(String name, Class<T> type, Table<R, K> table) {
        super(type, name, ColumnType.NORMAL, false, table);
    }

    public BinaryField(Class<T> type, String name, ColumnType columnType, boolean index, Table<R, K> table) {
        super(type, name, columnType, index, table);
    }

}
