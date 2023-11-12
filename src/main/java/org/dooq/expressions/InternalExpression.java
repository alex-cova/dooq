package org.dooq.expressions;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.engine.RendererContext;
import org.dooq.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

class InternalExpression<R extends AbstractRecord<R>, K extends Key> {

    private Operator operator;
    private final ExpressionRenderer<R, K> expression;

    public InternalExpression(ExpressionRenderer<R, K> expression) {
        this.expression = expression;
        this.operator = Operator.NONE;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public ExpressionRenderer<R, K> getExpression() {
        return expression;
    }

    boolean containsIndex(@Nullable Column<R, K> indexName) {
        return expression.containsIndex(indexName);
    }

    public Map<Column<R, K>, Object> getValue() {
        return expression.getValue();
    }

    public Map<String, String> getAttributeNames() {
        return expression.getAttributeNames();
    }

    public Map<String, AttributeValue> getAttributeValues() {
        return expression.getAttributeValues();
    }

    public void render(@NotNull RendererContext<R, K> context) {
        context.append(expression, operator);
    }

    public List<Column<R, K>> columns() {
        return expression.columns();
    }

}
