package com.dooq.api;

import com.dooq.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@ApiStatus.Experimental
public class FieldBuilder {

    public static <T, R extends AbstractRecord<R>, K extends Key> @NotNull Field<T, R, K>
    of(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return of(name, type, false, table);
    }

    @SuppressWarnings("all")
    public static <T, R extends AbstractRecord<R>, K extends Key> @NotNull Field<Set<T>, R, K>
    ofSet(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new Field(Set.class, name, ColumnType.NORMAL, false, table);
    }

    @SuppressWarnings("all")
    public static <T, V, R extends AbstractRecord<R>, K extends Key> @NotNull Field<Map<T, V>, R, K>
    ofMap(@NotNull String name, @NotNull Class<T> keyType, Class<V> valueType, Table<R, K> table) {
        return new Field(Map.class, name, ColumnType.NORMAL, false, table);
    }

    public static <T, R extends AbstractRecord<R>, K extends Key> @NotNull Field<T, R, K>
    of(@NotNull String name, @NotNull Class<T> type, boolean index, Table<R, K> table) {
        return new Field<>(type, name, ColumnType.NORMAL, index, table);
    }

    public static <T, R extends AbstractRecord<R>, K extends Key> @NotNull Field<T, R, K>
    partition(@NotNull String name, @NotNull Class<T> type,  Table<R, K> table) {
        return new Field<>(type, name, ColumnType.PARTITION, false, table);
    }

    public static <T, R extends AbstractRecord<R>, K extends Key> @NotNull Field<T, R, K>
    sort(@NotNull String name, @NotNull Class<T> type,  Table<R, K> table) {
        return new Field<>(type, name, ColumnType.SORT, false, table);
    }

    public static <T, R extends AbstractRecord<R>, K extends Key> @NotNull Field<T, R, K>
    partitionKey(@NotNull String name, Class<T> type, Table<R, K> table) {
        return new Field<>(type, name, ColumnType.PARTITION, false, table);
    }

    public static <T, R extends AbstractRecord<R>, K extends Key> @NotNull Field<T, R, K>
    sortKey(@NotNull String name, Class<T> type, Table<R, K> table) {
        return new Field<>(type, name, ColumnType.SORT, false, table);
    }
}
