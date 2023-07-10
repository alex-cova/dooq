package com.dooq.util;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.expressions.CompoundExpression;
import com.dooq.expressions.ConditionExpression;
import com.dooq.Key;
import com.dooq.api.Table;
import com.dooq.engine.ExpressionRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ExpressionResult<R extends AbstractRecord<R>, K extends Key> {

    private final Table<R, K> table;
    private String keyCondition;
    private String filterExpression;
    private Map<String, Object> expressionAttributeValues;
    private Map<String, String> attributeNames;
    private List<Column<R, K>> columns;
    private @Nullable Column<R, K> index;
    private List<ExpressionRenderer<R, K>> expressions;

    public ExpressionResult(Table<R, K> table) {
        this.table = table;
    }

    public ExpressionResult<R, K> setExpressions(List<ExpressionRenderer<R, K>> expressions) {
        this.expressions = expressions;
        return this;
    }

    public ExpressionResult<R, K> setKeyCondition(String keyCondition) {
        this.keyCondition = keyCondition;
        return this;
    }

    public ExpressionResult<R, K> setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
        return this;
    }

    public ExpressionResult<R, K> setExpressionAttributeValues(Map<String, Object> expressionAttributeValues) {
        this.expressionAttributeValues = expressionAttributeValues;
        return this;
    }

    public ExpressionResult<R, K> setAttributeNames(Map<String, String> attributeNames) {
        this.attributeNames = attributeNames;
        return this;
    }

    public ExpressionResult<R, K> setColumns(List<Column<R, K>> columns) {
        this.columns = columns;
        return this;
    }

    public ExpressionResult<R, K> setIndex(Column<R, K> index) {
        this.index = index;
        return this;
    }

    /**
     * Must know that fetching data from an LSI or GSI returns more than one row,
     * so this method must return false when an index is present.
     *
     * @return True if the columns size is equal to 2 and are the
     * partition and sort key.
     */
    public boolean isSimpleGet() {

        if (columns.size() != 2) return false;

        if (index != null) return false;

        if (expressions.size() > 2) return false;

        var onlyConditions = expressions.stream()
                .flatMap(e -> {
                    if (e instanceof CompoundExpression<R, K> expression) {
                        return expression.getExpressions().stream();
                    }
                    return Stream.of(e);
                }).allMatch(a -> a instanceof ConditionExpression);

        if (!onlyConditions) return false;

        var first = columns.get(0);
        var second = columns.get(1);

        return first.columnType().isKey() && second.columnType().isKey();
    }

    /**
     * @return The expected key to get a single item even when using a sort key
     */
    @Contract(" -> new")
    public @NotNull AbstractKey getComputedKey() {

        Column<?, ?> sortKey = table.getSortColumn();

        if (index != null) {
            sortKey = index;
        }

        var partitionValue = expressionAttributeValues.get(table.getPartitionColumn().param());
        var sortValue = expressionAttributeValues.get(sortKey.param());

        return new AbstractKey(table.getPartitionColumn(), partitionValue, sortKey, sortValue);
    }


    public Table<R, K> table() {
        return table;
    }

    public String keyCondition() {
        return keyCondition;
    }

    public String getFilterExpression() {
        return filterExpression;
    }

    public Map<String, Object> getExpressionAttributeValues() {
        return expressionAttributeValues;
    }

    public Map<String, String> attributeNames() {
        return attributeNames;
    }

    public List<Column<R, K>> columns() {
        return columns;
    }

    public @Nullable Column<R, K> index() {
        return index;
    }


}
