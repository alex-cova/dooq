package org.dooq.expressions;

import org.dooq.Key;
import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.engine.RendererContext;
import org.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;

/**
 * True if the attribute specified by path does not exist in the item.
 * <p>
 * Example: Check whether an item has a Manufacturer attribute.
 * <p>
 * attribute_not_exists (Manufacturer)
 */
public class AttributeNotExistsExpression<R extends DynamoRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    public AttributeNotExistsExpression(Column<R, K> column) {
        super(column);
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {
        context.append("attribute_not_exists (" + getColumn().escapedName() + ")", this);
    }

    @Override
    public @NotNull @Unmodifiable Map<Column<R, K>, Object> getValue() {
        return Collections.emptyMap();
    }
}
