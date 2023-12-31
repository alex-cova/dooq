package org.dooq.expressions;

import org.dooq.Key;
import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.engine.RendererContext;
import org.dooq.engine.SingleExpressionRenderer;
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
public class InExpression<T, R extends DynamoRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

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
