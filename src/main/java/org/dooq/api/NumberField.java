package org.dooq.api;

import org.dooq.Key;

public class NumberField<T, R extends AbstractRecord<R>, K extends Key> extends Field<T, R, K> {
    public NumberField(String name, Class<T> type, Table<R, K> table) {
        super(type, name, ColumnType.NORMAL, false, table);
    }

    public NumberField(Class<T> type, String name, ColumnType columnType, boolean index, Table<R, K> table) {
        super(type, name, columnType, index, table);
    }

}
