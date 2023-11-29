package org.dooq.join;

import org.dooq.api.Field;

public class MergeExpression<T> implements JoinExpression {
    private final Field<T, ?, ?> columnA;
    private final Field<T, ?, ?> columnB;

    public MergeExpression(Field<T, ?, ?> columnA, Field<T, ?, ?> columnB) {
        this.columnA = columnA;
        this.columnB = columnB;
    }

    public Field<T, ?, ?> getColumnA() {
        return columnA;
    }

    public Field<T, ?, ?> getColumnB() {
        return columnB;
    }

    public <K> FullMergeExpression<T, K> and(MergeExpression<K> expression) {
        return new FullMergeExpression<>(this, expression);
    }

}
