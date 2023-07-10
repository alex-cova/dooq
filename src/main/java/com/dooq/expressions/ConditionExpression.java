package com.dooq.expressions;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.engine.RendererContext;
import com.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * see: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.html
 */
public class ConditionExpression<R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private final Comparator comparator;
    private final Object value;

    public ConditionExpression(Column<R, K> column, Comparator comparator, Object value) {
        super(column);
        this.comparator = comparator;
        this.value = value;
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {
        context.append(getColumn().escapedName() + " " + comparator.getOperator() + " " + getColumn().param(), this);
    }

    @Override
    public @NotNull @Unmodifiable Map<Column<R, K>, Object> getValue() {
        return Map.of(getColumn(), value);
    }

}
