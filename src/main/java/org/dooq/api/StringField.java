package org.dooq.api;

import org.dooq.Key;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.expressions.BeginsWithExpression;

public class StringField<T, R extends DynamoRecord<R>, K extends Key> extends Field<T, R, K> {
    public StringField(String name, Class<T> type, Table<R, K> table) {
        super(type, name, ColumnType.NORMAL, false, table);
    }

    public StringField(Class<T> type, String name, ColumnType columnType, boolean index, Table<R, K> table) {
        super(type, name, columnType, index, table);
    }

    public ExpressionRenderer<R, K> startsWith(String value) {
        return new BeginsWithExpression<>(this, value);
    }
}
