package com.dooq.core.response;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Table;
import com.dooq.core.ListResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BufferedBatchGetItemResponse<R extends AbstractRecord<R>, K extends Key> implements ListResponse<R, K> {


    private final @Nullable BatchGetItemResponse response;
    private final Table<R, K> table;
    private final List<K> keyList;

    public BufferedBatchGetItemResponse(@Nullable BatchGetItemResponse response, Table<R, K> table, List<K> keyList) {
        this.response = response;
        this.table = table;
        this.keyList = keyList;
    }

    public BatchGetItemResponse response() {
        return response;
    }

    public Table<R, K> table() {
        return table;
    }

    public List<K> keyList() {
        return keyList;
    }

    public boolean anyPresent() {
        return !isEmpty();
    }

    public boolean allPresent() {

        if (response == null) return false;

        if (isEmpty()) return false;

        //Probably must check if every value match...
        return response.responses()
                .get(table.getTableName())
                .size() == keyList.size();
    }

    @Override
    public Table<R, K> getTable() {
        return table;
    }

    @Contract(pure = true)
    @Override
    public @NotNull @Unmodifiable Map<String, AttributeValue> lastEvaluatedKey() {
        return Collections.emptyMap();
    }

    @Override
    public List<Map<String, AttributeValue>> getItems() {

        if (response == null) return Collections.emptyList();

        return response.responses()
                .get(table.getTableName());

    }

    @Override
    public boolean isEmpty() {

        if (response == null) return true;

        List<Map<String, AttributeValue>> list = response.responses()
                .get(table.getTableName());

        if (list == null) return true;

        if (list.isEmpty()) return true;

        return list.stream()
                .noneMatch(Objects::nonNull);
    }

    @Override
    public boolean hasItems() {
        return !isEmpty();
    }

}
