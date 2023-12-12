package org.dooq.api;

import org.dooq.Key;
import org.dooq.expressions.BooleanComparison;

public class BooleanField<T, R extends DynamoRecord<R>, K extends Key> extends Field<T, R, K> {
    public BooleanField(String name, Class<T> type, Table<R, K> table) {
        super(type, name, ColumnType.NORMAL, false, table);
    }

    public BooleanField(Class<T> type, String name, ColumnType columnType, boolean index, Table<R, K> table) {
        super(type, name, columnType, index, table);
    }

    public BooleanComparison<R, K> isTrue() {
        return new BooleanComparison<>(this, true);
    }

    public BooleanComparison<R, K> isFalse() {
        return new BooleanComparison<>(this, false);
    }
}
