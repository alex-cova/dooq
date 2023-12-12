package org.dooq.engine;

import org.dooq.Key;
import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;

import java.util.List;

public abstract class SingleExpressionRenderer<R extends DynamoRecord<R>, K extends Key> extends AbstractExpressionRenderer<R, K> {

    private final Column<R, K> column;

    public SingleExpressionRenderer(Column<R, K> column) {
        this.column = column;
    }

    public Column<R, K> getColumn() {
        return column;
    }

    @Override
    public List<Column<R, K>> columns() {
        return List.of(column);
    }

}
