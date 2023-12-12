package org.dooq.api;

import org.dooq.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@ApiStatus.Experimental
public class FieldBuilder {

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull Field<T, R, K>
    of(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return of(name, type, false, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull NumberField<T, R, K>
    ofNumber(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new NumberField<>(name, type, table);
    }


    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull StringField<T, R, K>
    ofString(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new StringField<>(name, type, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull BooleanField<T, R, K>
    ofBoolean(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new BooleanField<>(name, type, table);
    }

    @SuppressWarnings("all")
    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull Field<Set<T>, R, K>
    ofSet(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new Field(Set.class, name, ColumnType.NORMAL, false, table);
    }

    @SuppressWarnings("all")
    public static <T, V, R extends DynamoRecord<R>, K extends Key> @NotNull Field<Map<T, V>, R, K>
    ofMap(@NotNull String name, @NotNull Class<T> keyType, Class<V> valueType, Table<R, K> table) {
        return new Field(Map.class, name, ColumnType.NORMAL, false, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull Field<T, R, K>
    of(@NotNull String name, @NotNull Class<T> type, boolean index, Table<R, K> table) {
        return new Field<>(type, name, ColumnType.NORMAL, index, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull Field<T, R, K>
    partition(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new Field<>(type, name, ColumnType.PARTITION, false, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull StringField<T, R, K>
    sort(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new StringField<>(type, name, ColumnType.SORT, false, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull NumberField<T, R, K>
    sortNumber(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new NumberField<>(type, name, ColumnType.SORT, false, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull BinaryField<T, R, K>
    sortBinary(@NotNull String name, @NotNull Class<T> type, Table<R, K> table) {
        return new BinaryField<>(type, name, ColumnType.SORT, false, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull Field<T, R, K>
    partitionKey(@NotNull String name, Class<T> type, Table<R, K> table) {
        return new Field<>(type, name, ColumnType.PARTITION, false, table);
    }

    public static <T, R extends DynamoRecord<R>, K extends Key> @NotNull Field<T, R, K>
    sortKey(@NotNull String name, Class<T> type, Table<R, K> table) {
        return new Field<>(type, name, ColumnType.SORT, false, table);
    }
}
