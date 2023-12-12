package org.dooq.expressions;

import org.dooq.Key;
import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.engine.RendererContext;
import org.dooq.engine.SingleExpressionRenderer;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class NullComparisonExpression<R extends DynamoRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private boolean negated;

    public NullComparisonExpression(Column<R, K> column) {
        super(column);
    }

    public NullComparisonExpression(Column<R, K> column, boolean negated) {
        super(column);
        this.negated = negated;
    }

    @Override
    public void render(RendererContext<R, K> context) {

        if (negated) {
            context.append("attribute_exists (" + getColumn().escapedName() + ")", this);
            return;
        }

        context.append("attribute_type (" + getColumn().escapedName() + ", " + "NULL" + ")", this);
    }

    @Override
    public Map<String, AttributeValue> getAttributeValues() {
        return Map.of();
    }

    @Override
    public Map<Column<R, K>, Object> getValue() {
        return Map.of(getColumn(), Void.class);
    }
}
