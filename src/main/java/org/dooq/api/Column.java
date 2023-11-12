package org.dooq.api;

import org.dooq.core.DynamoSemantics;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.Key;
import org.dooq.expressions.*;
import org.dooq.join.EqualsColumnExpression;
import org.dooq.join.JoinExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Column<R extends AbstractRecord<R>, K extends Key> {

    String name();

    ColumnType columnType();

    boolean index();

    Table<R, K> table();

    default boolean isKey(@Nullable Column<?, ?> index) {

        if (index == null) {
            return columnType().isKey();
        }

        if (index.name().equals(name())) return true;

        var indexSpecification = table().getIndex(index.name());

        if (indexSpecification == null) return false;

        if (indexSpecification.type() == IndexType.LOCAL) {
            return indexSpecification.isKey(this) || columnType().isKey();
        }

        return indexSpecification.isKey(this);
    }

    default boolean equalsIndexName(String index) {
        if (index == null) {
            return false;
        }

        return name().equals(index);
    }

    default String escapedName() {
        return DynamoSemantics.escaped(name());
    }

    default String param() {
        return DynamoSemantics.param(this);
    }

    default ExpressionRenderer<R, K> startsWith(String value) {
        return new BeginsWithExpression<>(this, value);
    }

    default <A extends AbstractRecord<A>, B extends Key> JoinExpression<A, B> startsWith(Column<A, B> column) {
        return new EqualsColumnExpression<>(this, column);
    }

    default ExpressionRenderer<R, K> attributeExists() {
        return new AttributeExistsExpression<>(this);
    }

    default ExpressionRenderer<R, K> attributeNotExists() {
        return new AttributeNotExistsExpression<>(this);
    }

    default ExpressionRenderer<R, K> contains(String value) {
        return new ContainsExpression<>(this, value);
    }

    default ExpressionRenderer<R, K> eq(Object obj) {
        return new ConditionExpression<>(this, Comparator.EQUALS, obj);
    }

    default ExpressionRenderer<R, K> lessThan(Object obj) {
        return new ConditionExpression<>(this, Comparator.LESS, obj);
    }

    default ExpressionRenderer<R, K> lessOrEqual(Object obj) {
        return new ConditionExpression<>(this, Comparator.LESS_OR_EQUAL, obj);
    }

    default ExpressionRenderer<R, K> greaterThan(Object obj) {
        return new ConditionExpression<>(this, Comparator.GREATER, obj);
    }

    default ExpressionRenderer<R, K> greaterOrEqual(Object obj) {
        return new ConditionExpression<>(this, Comparator.GREATER_OR_EQUAL, obj);
    }

    default BetweenExpression.PreBetweenExpression<R, K> between(Object a) {
        return new BetweenExpression.PreBetweenExpression<>(this, a);
    }


    default BooleanComparison<R, K> isTrue() {
        return new BooleanComparison<>(this);
    }

    default BooleanComparison<R, K> isFalse() {
        return new BooleanComparison<>(this, false);
    }

    default <T> InExpression<T, R, K> in(List<T> values) {
        return new InExpression<>(this, values);
    }
}
