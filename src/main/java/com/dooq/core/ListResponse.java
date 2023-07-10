package com.dooq.core;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.Key;
import com.dooq.api.Table;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ListResponse<R extends AbstractRecord<R>, K extends Key> extends Response {

    Table<R, K> getTable();

    Map<String, AttributeValue> lastEvaluatedKey();

    List<Map<String, AttributeValue>> getItems();

    default List<R> items() {
        return into(getTable().getRecordType());
    }

    default R one() {
        return oneInto(getTable().getRecordType());
    }

    default <T> List<T> into(Class<T> type) {

        if (isEmpty()) return Collections.emptyList();

        return getItems().stream()
                .map(map -> ItemParser.readRecord(map, type))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    default @Nullable String getLastEvaluatedKey(Column<R, K> column) {
        return getLastKey().get(column);
    }

    @Contract(" -> new")
    default @NotNull LastEvaluatedKey getLastKey() {
        return new LastEvaluatedKey(lastEvaluatedKey());
    }

    default <T> @Nullable T oneInto(Class<T> type) {

        if (getItems().isEmpty()) {
            return null;
        }

        return ItemParser.readRecord(getItems().get(0), type);
    }
}
