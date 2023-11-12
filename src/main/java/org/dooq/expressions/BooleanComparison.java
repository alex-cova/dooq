package org.dooq.expressions;

import org.dooq.engine.RendererContext;
import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class BooleanComparison<R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private final boolean value;

    public BooleanComparison(Column<R, K> column) {
        super(column);
        this.value = true;
    }

    public BooleanComparison(Column<R, K> column, boolean value) {
        super(column);
        this.value = value;
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {

        String result = getColumn().name() + " = " + Boolean.toString(value).toUpperCase();

        context.append(result, this);
    }

    @Override
    public Map<String, AttributeValue> getAttributeValues() {
        return Map.of();
    }

    @Override
    public Map<Column<R, K>, Object> getValue() {
        return Map.of(getColumn(), value);
    }

}
