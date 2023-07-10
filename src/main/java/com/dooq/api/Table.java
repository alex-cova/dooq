package com.dooq.api;


import com.dooq.Key;
import com.dooq.core.schema.Index;
import com.dooq.engine.ExpressionRenderer;
import com.dooq.join.JoinExpression;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface Table<R extends AbstractRecord<R>, K extends Key> {

    List<Column<R, K>> getColumns();

    String getTableName();

    Class<R> getRecordType();

    Column<R, K> getPartitionColumn();

    Column<R, K> getSortColumn();

    @ApiStatus.Experimental
    default <A extends AbstractRecord<A>, B extends Key> JoinExpression<A, B> on(JoinExpression<A, B> expression) {
        return expression;
    }

    default Map<String, Index> getIndices() {
        return Collections.emptyMap();
    }

    default @Nullable Index getIndex(String name) {
        return getIndices().get(name);
    }

    @SuppressWarnings("unchecked")
    default List<Column<R, K>> columns() {
        return Arrays.stream(getClass().getDeclaredFields())
                .filter(a -> a.getType() == Field.class)
                .map(a -> {
                    try {
                        return (Column<R, K>) a.get(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
