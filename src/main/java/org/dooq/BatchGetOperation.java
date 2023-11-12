package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.api.Table;
import org.dooq.core.response.BufferedBatchGetItemResponse;
import org.dooq.core.DynamoOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * You should use BatchGetItem if you need to retrieve many items with little HTTP overhead when compared to GetItem.
 * <p>
 * A BatchGetItem costs the same as calling GetItem for each individual item. However, it can be faster since you are making fewer network requests.
 * <p>
 * https://stackoverflow.com/questions/30749560/whats-the-difference-between-batchgetitem-and-query-in-dynamodb
 *
 * @param <R>
 * @param <K>
 */
public class BatchGetOperation<R extends AbstractRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private DynamoDbClient client;
    private final BatchGetItemRequest.Builder builder;
    private final List<K> keyList;
    private final List<Column<R, K>> columns;
    private boolean debug;
    private boolean consistent;

    public BatchGetOperation(Table<R, K> table) {
        super(table);

        columns = new ArrayList<>();
        builder = BatchGetItemRequest.builder();
        keyList = new ArrayList<>();
    }

    public BatchGetOperation(Table<R, K> table, DynamoDbClient client) {
        this(table, new ArrayList<>());
        this.client = client;
    }

    public BatchGetOperation(Table<R, K> table, List<Column<R, K>> columns) {
        super(table);

        this.columns = columns;
        builder = BatchGetItemRequest.builder();
        keyList = new ArrayList<>();
    }

    public BatchGetOperation<R, K> select(Column<R, K> column) {
        this.columns.add(column);
        return this;
    }

    @SafeVarargs
    public final BatchGetOperation<R, K> select(Column<R, K>... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    public BatchGetOperation<R, K> strip() {
        this.columns.clear();
        this.columns.add(getTable().getPartitionColumn());

        return this;
    }

    public BatchGetOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public BatchGetOperation<R, K> setColumns(List<Column<R, K>> columns) {

        this.columns.clear();
        this.columns.addAll(columns);

        return this;
    }

    public BatchGetOperation<R, K> consistent() {
        consistent = true;
        return this;
    }

    public BatchGetOperation<R, K> get(K key) {
        keyList.add(key);

        return this;
    }

    public BatchGetOperation<R, K> get(K key, K key2) {
        keyList.add(key);
        keyList.add(key2);

        return this;
    }

    public BatchGetOperation<R, K> get(List<K> keys) {
        keyList.addAll(keys);
        return this;
    }

    public static <R extends AbstractRecord<R>, K extends Key> @NotNull BatchGetOperation<R, K> from(Table<R, K> table) {
        return new BatchGetOperation<>(table);
    }

    public List<R> fetch() {
        return execute()
                .items();
    }

    public Stream<R> stream() {
        return fetch().stream();
    }

    public BatchGetOperation<R, K> debug() {
        this.debug = true;

        return this;
    }


    //Limit 100 keys per request
    public BufferedBatchGetItemResponse<R, K> execute() {

        if (keyList.size() < 100) {
            return new BufferedBatchGetItemResponse<>(execute(keyList), getTable(), new ArrayList<>(keyList));
        }

        var keys = new ArrayList<>(keyList);

        BatchGetItemResponse response;
        Map<String, List<Map<String, AttributeValue>>> values = new HashMap<>();

        for (int i = 0; i < keyList.size(); i += 100) {
            var subList = keys.subList(i, Math.min(i + 100, keyList.size()));

            response = execute(subList);

            if (response == null || response.responses() == null) continue;

            values.putAll(response.responses());
        }

        BatchGetItemResponse itemResponse = BatchGetItemResponse.builder()
                .responses(values)
                .build();

        return new BufferedBatchGetItemResponse<>(itemResponse, getTable(), new ArrayList<>(keyList));
    }

    private @Nullable BatchGetItemResponse execute(@NotNull Collection<K> keyList) {

        if (keyList.isEmpty()) {
            return null;
        }

        Map<String, KeysAndAttributes> keyMap = new HashMap<>();

        KeysAndAttributes.Builder keysAndAttributes;

        if (columns.isEmpty()) {
            keysAndAttributes = KeysAndAttributes.builder()
                    .consistentRead(consistent)
                    .keys(keyList);
        } else {
            keysAndAttributes = KeysAndAttributes.builder()
                    .consistentRead(consistent)
                    .attributesToGet(columns.stream()
                            .map(Column::name)
                            .collect(Collectors.toList()))
                    .keys(keyList);
        }

        keyMap.put(getTable().getTableName(), keysAndAttributes.build());

        builder.requestItems(keyMap);

        var build = builder.build();

        if (debug) {
            System.out.println(build);
        }

        return client.batchGetItem(build);
    }

    public <T> List<T> fetchInto(Class<T> type) {
        return execute()
                .into(type);
    }
}
