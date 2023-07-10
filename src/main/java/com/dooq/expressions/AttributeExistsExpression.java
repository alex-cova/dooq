package com.dooq.expressions;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.engine.RendererContext;
import com.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;

/**
 * True if the item contains the attribute specified by path.
 * <p>
 * Example: Check whether an item in the Product table has a side view picture.
 * <p>
 * attribute_exists (#Pictures.#SideView)
 */
public class AttributeExistsExpression<R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    public AttributeExistsExpression(Column<R, K> column) {
        super(column);
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {
        context.append("attribute_exists (" + getColumn().escapedName() + ")", this);
    }

    @Override
    public @NotNull @Unmodifiable Map<Column<R, K>, Object> getValue() {
        return Collections.emptyMap();
    }
}
