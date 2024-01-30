package org.dooq.expressions;

import org.dooq.Key;
import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.core.Expression;

public class ListAppendExpression<R extends DynamoRecord<R>, K extends Key> implements Expression {

    private final Column<R, K> column;

    public ListAppendExpression(Column<R, K> column) {
        this.column = column;
    }

    @Override
    public Column<?, ?> column() {
        return column;
    }

    //#fs = list_append(#fs, :fs)
    @Override
    public String render() {
        return "%s = list_append(%s, %s)".formatted(column.escapedName(), column.escapedName(), column.param());
    }
}
