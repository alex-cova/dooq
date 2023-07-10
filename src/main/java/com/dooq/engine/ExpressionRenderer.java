package com.dooq.engine;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.api.ColumnType;
import com.dooq.expressions.CompoundExpression;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * condition-expression ::=
 * operand comparator operand
 * | operand BETWEEN operand AND operand
 * | operand IN ( operand (',' operand (, ...) ))
 * | function
 * | condition AND condition
 * | condition OR condition
 * | NOT condition
 * | ( condition )
 * <p>
 * comparator ::=
 * =
 * | <>
 * | <
 * | <=
 * | >
 * | >=
 * <p>
 * function ::=
 * attribute_exists (path)
 * | attribute_not_exists (path)
 * | attribute_type (path, type)
 * | begins_with (path, substr)
 * | contains (path, operand)
 * | size (path)
 */
public interface ExpressionRenderer<R extends AbstractRecord<R>, K extends Key> {

    List<Column<R, K>> columns();

    void render(RendererContext<R, K> context);

    boolean containsIndex(@Nullable Column<R, K> indexName);

    /**
     * @return Un serialized version of AttributeValues
     */
    Map<Column<R, K>, Object> getValue();

    Map<String, AttributeValue> getAttributeValues();

    Map<String, String> getAttributeNames();

    default List<ColumnType> getTypes() {
        return columns().stream()
                .map(Column::columnType)
                .collect(Collectors.toList());
    }

    default boolean containsKey(@Nullable Column<R, K> index) {
        return columns().stream()
                .anyMatch(a -> a.isKey(index));
    }

    default CompoundExpression<R, K> and(ExpressionRenderer<R, K> renderer) {
        return new CompoundExpression<>(this)
                .and(renderer);
    }

    default CompoundExpression<R, K> or(ExpressionRenderer<R, K> renderer) {
        return new CompoundExpression<>(this)
                .or(renderer);
    }

}
