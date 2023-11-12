package org.dooq.expressions;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.engine.RendererContext;
import org.dooq.Key;
import org.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * True if the attribute specified by path begins with a particular substring.
 * <p>
 * Example: Check whether the first few characters of the front view picture URL are http://.
 * <p>
 * begins_with (Pictures.FrontView, :v_sub)
 * The expression attribute value :v_sub is a placeholder for http://.
 */
public class BeginsWithExpression<R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private final String prefix;

    public BeginsWithExpression(Column<R, K> column, String prefix) {
        super(column);
        this.prefix = prefix;
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {
        context.append("begins_with(" + getColumn().escapedName() + ", " + getColumn().param() + ")", this);
    }

    @Override
    public @NotNull @Unmodifiable Map<Column<R, K>, Object> getValue() {
        return Map.of(getColumn(), prefix);
    }
}
