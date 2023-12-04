package org.dooq.core;

import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.api.Table;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface SingleResponse<R extends AbstractRecord<R>, K extends Key> extends Response {

    Map<String, AttributeValue> getItem();

    Table<R, K> getTable();

    default <T> @Nullable T getAttribute(@NotNull Column<R, K> column, Function<AttributeValue, T> function) {
        var value = getItem().get(column.name());

        if (value == null) return null;

        return function.apply(value);
    }

    @SuppressWarnings("unchecked")
    default <T> @Nullable T into(Class<T> clazz) {

        if (isEmpty()) {
            return null;
        }

        if (clazz == getTable().getRecordType()) {
            return (T) getTable()
                    .getRecordParser()
                    .read(getItem());
        }

        return ItemParser.readRecord(getItem(), clazz);
    }

    default <T> T get(Column<?, ?> column, Function<AttributeValue, T> function) {

        if (isEmpty()) throw new IllegalStateException("No item");

        var attributeValue = getItem()
                .get(column.name());

        return function.apply(attributeValue);
    }

    @Contract(value = "_ -> new", pure = true)
    static <R extends AbstractRecord<R>, K extends Key> @NotNull ListResponse<R, K> asListResponse(SingleResponse<R, K> response) {
        return new ListResponse<>() {
            @Override
            public Table<R, K> getTable() {
                return response.getTable();
            }

            @Override
            public Map<String, AttributeValue> lastEvaluatedKey() {
                return Collections.emptyMap();
            }

            @Override
            public List<Map<String, AttributeValue>> getItems() {
                return List.of(response.getItem());
            }

            @Override
            public boolean isEmpty() {
                return response.isEmpty();
            }

            @Override
            public boolean hasItems() {
                return response.hasItems();
            }
        };
    }
}
