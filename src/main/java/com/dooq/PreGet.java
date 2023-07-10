package com.dooq;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.api.Table;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PreGet<R extends AbstractRecord<R>, K extends Key> {

    private final List<Column<R, K>> columnList;
    private DynamoDbClient client;

    public PreGet(List<Column<R, K>> columnList) {
        this.columnList = columnList;
    }

    public PreGet<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    @SafeVarargs
    @Contract("_ -> new")
    public static @NotNull <R extends AbstractRecord<R>, K extends Key> PreGet<R, K> get(Column<R, K>... columns) {
        return new PreGet<>(Arrays.asList(columns));
    }

    public static @NotNull <R extends AbstractRecord<R>, K extends Key> PreGet<R, K> get(Column<R, K> column) {
        return new PreGet<>(Collections.singletonList(column));
    }

    public static @NotNull <R extends AbstractRecord<R>, K extends Key> PreGet<R, K> get(List<Column<R, K>> columns) {
        return new PreGet<>(columns);
    }

    @Contract("_ -> new")
    public static <R extends AbstractRecord<R>, K extends Key> GetOperation<R, K> selectFrom(@NotNull Table<R, K> table) {
        return new PreGet<>(table.getColumns())
                .from(table);
    }

    public BatchGetOperation<R, K> batch(Table<R, K> table) {
        return new BatchGetOperation<>(table, columnList);
    }

    public GetOperation<R, K> from(Table<R, K> table) {
        return new GetOperation<>(table, columnList)
                .setClient(client);
    }

}
