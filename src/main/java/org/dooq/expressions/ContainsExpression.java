package org.dooq.expressions;

import org.dooq.engine.RendererContext;
import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * True if the attribute specified by path is one of the following:
 * <p>
 * A String that contains a particular substring.
 * A Set that contains a particular element within the set.
 * The operand must be a String if the attribute specified by path is a String. If the attribute specified by path is a Set, the operand must be the set's element type.
 * <p>
 * The path and the operand must be distinct; that is, contains (a, a) returns an error.
 * <p>
 * Example: Check whether the Brand attribute contains the substring Company.
 * <p>
 * contains (Brand, :v_sub)
 * The expression attribute value :v_sub is a placeholder for Company.
 * <p>
 * Example: Check whether the product is available in red.
 * <p>
 * contains (Color, :v_sub)
 * The expression attribute value :v_sub is a placeholder for Red.
 */
public class ContainsExpression<R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private final String value;

    public ContainsExpression(Column<R, K> column, String value) {
        super(column);
        this.value = value;
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {
        context.append("contains(" + getColumn().escapedName() + ", " + getColumn().param() + ")", this);
    }

    @Override
    public @NotNull @Unmodifiable Map<Column<R, K>, Object> getValue() {
        return Map.of(getColumn(), value);
    }
}
