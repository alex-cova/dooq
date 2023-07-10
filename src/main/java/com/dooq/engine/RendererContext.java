package com.dooq.engine;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.api.Table;
import com.dooq.expressions.CompoundExpression;
import com.dooq.expressions.Operator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RendererContext<R extends AbstractRecord<R>, K extends Key> {

    private final Table<R, K> table;
    private final @Nullable Column<R, K> index;
    private final List<String> keyExpression = new ArrayList<>();
    private final List<String> filterExpression = new ArrayList<>();

    public RendererContext(Table<R, K> table, @Nullable Column<R, K> index) {
        this.table = table;
        this.index = index;
    }

    public Table<R, K> getTable() {
        return table;
    }

    public @Nullable Column<R, K> getIndex() {
        return index;
    }

    public void append(String rendered, ExpressionRenderer<R, K> expression) {
        if (isKey(expression)) {
            keyExpression.add(rendered);
        } else {
            filterExpression.add(rendered);
        }
    }

    public void append(@NotNull ExpressionRenderer<R, K> expression, @Nullable Operator operator) {

        if (expression instanceof CompoundExpression<R, K> compound) {

            var noKeys = !expression.containsKey(index);

            if (noKeys) filterExpression.add("(");

            compound.render(this);

            if (noKeys) filterExpression.add(")");

            return;
        }
        expression.render(this);

        if (operator != null) {
            if (isKey(expression)) {
                keyExpression.add(operator.getValue());
            } else {
                filterExpression.add(operator.getValue());
            }
        }


    }

    public String getKeyExpression() {
        return cleanUp(keyExpression);
    }

    public String getFilterExpression() {
        return cleanUp(filterExpression);
    }

    @Nullable
    private String cleanUp(@NotNull List<String> expressionList) {
        if (expressionList.isEmpty()) return null;

        if (Operator.isOperator(expressionList.get(0))) {
            expressionList.remove(0);

        }

        if (expressionList.isEmpty()) return null;

        if (Operator.isOperator(expressionList.get(expressionList.size() - 1))) {
            expressionList.remove(expressionList.size() - 1);
        }

        if (expressionList.isEmpty()) return null;

        return String.join(" ", expressionList)
                .trim();
    }

    public boolean isKey(@NotNull ExpressionRenderer<R, K> expression) {
        if (expression.columns().size() == 1) {
            Column<R, K> column = expression.columns().get(0);

            return column.isKey(index);
        }

        throw new IllegalStateException("Must not compare a multi column expression: " + expression.getClass());
    }

}
