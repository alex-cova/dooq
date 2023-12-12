package org.dooq.api;

import org.dooq.Key;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.expressions.Comparator;
import org.dooq.expressions.ConditionExpression;
import org.dooq.join.MergeExpression;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Field<T, R extends DynamoRecord<R>, K extends Key> implements Column<R, K>, FieldType<T> {

    private final Class<T> type;
    private final String name;
    private final ColumnType columnType;
    private final boolean index;
    private final Table<R, K> table;

    public Field(Class<T> type, String name, ColumnType columnType, boolean index, Table<R, K> table) {
        this.type = type;
        this.name = name;
        this.columnType = columnType;
        this.index = index;
        this.table = table;
    }

    public Class<T> type() {
        return type;
    }

    public String name() {
        return name;
    }

    public ColumnType columnType() {
        return columnType;
    }

    public boolean index() {
        return index;
    }

    public Table<R, K> table() {
        return table;
    }

    public boolean isAssignableFrom(Class<?> type) {
        return this.type.isAssignableFrom(type);
    }

    public boolean isCollection() {
        return List.class.isAssignableFrom(type) ||
                Set.class.isAssignableFrom(type) ||
                Collection.class.isAssignableFrom(type);
    }

    @Contract("_ -> new")
    public @NotNull ExpressionRenderer<R, K> eq(@NotNull T obj) {
        return new ConditionExpression<>(this, Comparator.EQUALS, obj);
    }

    @Contract(pure = true)
    public @Nullable MergeExpression<T> eq(@NotNull Field<T, ?, ?> field) {
        return null;
        //return new EqualsColumnExpression<>(this, field);
    }

    @Contract(pure = true)
    public @Nullable MergeExpression<T> startsWith(@NotNull Field<T, ?, ?> column) {
        return null;
    }

}
