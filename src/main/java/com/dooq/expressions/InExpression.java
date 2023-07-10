package com.dooq.expressions;

import com.dooq.engine.RendererContext;
import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

//https://stackoverflow.com/questions/43476496/query-dynamodb-with-in-clause

/**
 * This is a query filter
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LegacyConditionalParameters.QueryFilter.html
 *
 * @param <T>
 */
public class InExpression<T, R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private final List<T> values;

    public InExpression(Column<R, K> column, List<T> values) {
        super(column);
        this.values = values;
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {

        if (values.size() == 1) {
            context.append(getColumn().escapedName() + " = " + getColumn().param(), this);
            return;
        }

        context.append(getColumn().escapedName() + " IN (" + getColumn().param() + ")", this);
    }

    @Override
    public @NotNull @Unmodifiable Map<Column<R, K>, Object> getValue() {

        if (values.size() == 1) {
            return Map.of(getColumn(), values.get(0));
        }

        return Map.of(getColumn(), values);
    }


}
