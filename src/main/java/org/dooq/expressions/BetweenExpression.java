package org.dooq.expressions;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.core.AttributeWriter;
import org.dooq.engine.RendererContext;
import org.dooq.Key;
import org.dooq.engine.SingleExpressionRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.Map;

public class BetweenExpression<R extends AbstractRecord<R>, K extends Key> extends SingleExpressionRenderer<R, K> {

    private final Object a;
    private final Object b;

    public BetweenExpression(Column<R, K> column, Object a, Object b) {
        super(column);
        this.a = a;
        this.b = b;
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {

        String suffix = getColumn().name()
                .substring(0, Math.min(getColumn().name().length(), 3));

        String result = getColumn().escapedName() + " BETWEEN :a" + suffix + " AND :b" + suffix;

        context.append(result, this);
    }

    @Contract(pure = true)
    @Override
    public @Unmodifiable Map<Column<R, K>, Object> getValue() {
        return Collections.emptyMap();
    }

    @Override
    public @NotNull @Unmodifiable Map<String, AttributeValue> getAttributeValues() {
        String prefix = getColumn().name().substring(0, Math.min(getColumn().name().length(), 3));

        return Map.of(":a" + prefix, AttributeWriter.parse(a), ":b" + prefix, AttributeWriter.parse(b));
    }

    public static class PreBetweenExpression<R extends AbstractRecord<R>, K extends Key> {

        private final Column<R, K> column;
        private final Object a;

        public PreBetweenExpression(Column<R, K> column, Object a) {
            this.column = column;
            this.a = a;
        }

        @Contract("_ -> new")
        public @NotNull BetweenExpression<R, K> and(Object b) {
            return new BetweenExpression<>(column, a, b);
        }
    }
}
