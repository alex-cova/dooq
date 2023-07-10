package com.dooq.api;

import com.dooq.Key;

public record Field<T, R extends AbstractRecord<R>, K
        extends Key>(Class<T> type, String name, ColumnType columnType, boolean index,
                     Table<R, K> table) implements Column<R, K>, FieldType<T> {


}