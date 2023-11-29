package org.dooq.expressions;

import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.engine.RendererContext;
import org.dooq.engine.SingleExpressionRenderer;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class AttributeTypeExpression<R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private final AttributeType type;

    public AttributeTypeExpression(Column<R, K> column, AttributeType type) {
        super(column);
        this.type = type;
    }

    @Override
    public void render(RendererContext<R, K> context) {
        context.append("attribute_type (" + getColumn().escapedName() + ", " + type.name() + ")", this);
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

