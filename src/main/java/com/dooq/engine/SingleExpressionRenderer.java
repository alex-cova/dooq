package com.dooq.engine;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.Key;

import java.util.List;

public abstract class SingleExpressionRenderer<R extends AbstractRecord<R>, K extends Key> extends AbstractExpressionRenderer<R, K> {

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
