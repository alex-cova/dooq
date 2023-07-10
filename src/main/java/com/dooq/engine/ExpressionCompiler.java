package com.dooq.engine;

import com.dooq.expressions.CompoundExpression;
import com.dooq.util.ScanExpressionResult;
import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.api.Table;
import com.dooq.util.AbstractKey;
import com.dooq.util.ExpressionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExpressionCompiler {

    public static <R extends AbstractRecord<R>, K extends Key> @NotNull ScanExpressionResult<R, K>
    compileForScan(@NotNull Table<R, K> table, @NotNull List<ExpressionRenderer<R, K>> list, Column<R, K> index) {
        var compiled = compile(table, list, index);

        List<String> expressions = List.of(compiled.getFilterExpression(), compiled.keyCondition());

        return new ScanExpressionResult<>(String.join(" AND ", expressions),
                compiled.getExpressionAttributeValues(), compiled.attributeNames(), compiled.columns());
    }

    /**
     * Each KeyConditions element consists of an attribute name to compare, along with the following:
     * <p>
     * AttributeValueList - One or more values to evaluate against the supplied attribute. The number of values in the
     * list depends on the ComparisonOperator being used.
     * For type Number, value comparisons are numeric.
     * String value comparisons for greater than, equals, or less than are based on Unicode with UTF-8 binary encoding.
     * For example, a is greater than A, and a is greater than B.
     * For Binary, DynamoDB treats each byte of the binary data as unsigned when it compares binary values.
     * ComparisonOperator - A comparator for evaluating attributes, for example, equals, greater than, less than, and so on.
     * For KeyConditions, only the following comparison operators are supported:
     * EQ | LE | LT | GE | GT | BEGINS_WITH | BETWEEN
     */
    public static <R extends AbstractRecord<R>, K extends Key> @NotNull ExpressionResult<R, K>
    compile(Table<R, K> table, @NotNull List<ExpressionRenderer<R, K>> expressionList, @Nullable Column<R, K> index) {

        final var columns = expressionList
                .stream()
                .flatMap(a -> a.columns().stream())
                .collect(Collectors.toList());

        RendererContext<R, K> context = new RendererContext<>(table, index);

        final Map<String, String> attributeNames = new HashMap<>();

        for (ExpressionRenderer<R, K> expression : expressionList) {
            attributeNames.putAll(expression.getAttributeNames());
            expression.render(context);
        }

        var resultMap = map(expressionList);

        return new ExpressionResult<>(table)
                .setExpressions(expressionList)
                .setKeyCondition(context.getKeyExpression())
                .setFilterExpression(context.getFilterExpression())
                .setAttributeNames(attributeNames)
                .setExpressionAttributeValues(resultMap)
                .setIndex(index)
                .setColumns(columns);

    }

    public static <R extends AbstractRecord<R>, K extends Key> Key buildKey(Table<R, K> table, ExpressionRenderer<R, K> expression) {

        if (expression instanceof CompoundExpression<R, K> compound) {

            var partition = compound.getPartitionKey(table);
            var sort = compound.getSortKey(table);

            Objects.requireNonNull(partition, "Partition Key not found");

            return new AbstractKey()
                    .setPartitionKeyName(partition)
                    .setSortingKey(sort);
        }

        var map = expression.getValue();

        return new AbstractKey()
                .setPartitionKeyName(map);
    }

    public static <R extends AbstractRecord<R>, K extends Key>
    @NotNull Map<String, Object> map(@NotNull List<ExpressionRenderer<R, K>> expressionList) {
        Map<String, Object> resultMap = new HashMap<>();

        for (ExpressionRenderer<?, ?> exp : expressionList) {

            var value = exp.getValue();

            for (Column<?, ?> s : value.keySet()) {
                if (resultMap.containsKey(s.name())) {
                    throw new IllegalStateException("Repeated key: " + s);
                }

                resultMap.put(s.param(), value.get(s));
            }
        }

        return resultMap;
    }
}
