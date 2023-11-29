package org.dooq.expressions;

import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.api.Table;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.engine.RendererContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CompoundExpression<R extends AbstractRecord<R>, K extends Key> implements ExpressionRenderer<R, K> {

    private final List<InternalExpression<R, K>> expressions;

    public CompoundExpression(ExpressionRenderer<R, K> expression) {
        this.expressions = new ArrayList<>();
        this.expressions.add(new InternalExpression<>(expression));
    }

    public int size() {
        return expressions.size();
    }

    public List<ExpressionRenderer<R, K>> getExpressions() {
        return expressions.stream()
                .map(InternalExpression::getExpression)
                .toList();
    }

    @Override
    public CompoundExpression<R, K> and(@NotNull ExpressionRenderer<R, K> expression) {
        setLast(Operator.AND);

        expressions.add(new InternalExpression<>(expression));
        return this;
    }

    @Override
    public CompoundExpression<R, K> or(@NotNull ExpressionRenderer<R, K> expression) {
        setLast(Operator.OR);

        expressions.add(new InternalExpression<>(expression));
        return this;
    }

    public CompoundExpression<R, K> not(ExpressionRenderer<R, K> expression) {
        setLast(Operator.NOT);

        expressions.add(new InternalExpression<>(expression));
        return this;
    }

    private void setLast(Operator operator) {
        if (expressions.isEmpty()) return;

        expressions.get(expressions.size() - 1)
                .setOperator(operator);
    }

    @Override
    public List<Column<R, K>> columns() {
        return expressions.stream()
                .flatMap(a -> a.columns().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void render(@NotNull RendererContext<R, K> context) {
        for (InternalExpression<R, K> expression : expressions) {
            expression.render(context);
        }
    }

    @Override
    public boolean containsIndex(@Nullable Column<R, K> indexName) {
        return expressions
                .stream()
                .anyMatch(a -> a.containsIndex(indexName));
    }

    @Override
    public @NotNull Map<Column<R, K>, Object> getValue() {
        Map<Column<R, K>, Object> resultMap = new HashMap<>();

        for (InternalExpression<R, K> expression : expressions) {
            resultMap.putAll(expression.getValue());
        }

        return resultMap;
    }

    @Override
    public @NotNull Map<String, AttributeValue> getAttributeValues() {
        Map<String, AttributeValue> resultMap = new HashMap<>();

        for (InternalExpression<R, K> expression : expressions) {
            resultMap.putAll(expression.getAttributeValues());
        }

        return resultMap;
    }

    @Override
    public @NotNull Map<String, String> getAttributeNames() {
        Map<String, String> resultMap = new HashMap<>();

        for (InternalExpression<R, K> expression : expressions) {
            resultMap.putAll(expression.getAttributeNames());
        }

        return resultMap;
    }

    public @Nullable @Unmodifiable Map<Column<R, K>, Object> getPartitionKey(@NotNull Table<R, K> table) {
        return search(table.getPartitionColumn());
    }

    public @Nullable @Unmodifiable Map<Column<R, K>, Object> getSortKey(@NotNull Table<R, K> table) {
        return search(table.getSortColumn());
    }

    public @Nullable @Unmodifiable Map<Column<R, K>, Object> search(Column<R, K> targetColumn) {

        for (InternalExpression<R, K> expression : expressions) {
            for (Column<R, K> column : expression.getExpression().columns()) {

                if (targetColumn.name().equals(column.name())) {

                    Map<Column<R, K>, Object> value = expression.getExpression()
                            .getValue();

                    return Map.of(column, value.get(column));
                }
            }
        }

        return null;
    }

}
