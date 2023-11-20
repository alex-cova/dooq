package org.dooq.api;

import org.dooq.Key;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record Field<T, R extends AbstractRecord<R>, K
        extends Key>(Class<T> type, String name, ColumnType columnType, boolean index, Table<R, K> table)
        implements Column<R, K>, FieldType<T> {

    public boolean isAssignableFrom(Class<?> type) {
        return this.type.isAssignableFrom(type);
    }

    public boolean isCollection() {
        return List.class.isAssignableFrom(type) ||
                Set.class.isAssignableFrom(type) ||
                Collection.class.isAssignableFrom(type);
    }
}
