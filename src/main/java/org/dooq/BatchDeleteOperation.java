package org.dooq;

import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.DynamoOperation;
import org.dooq.core.response.BufferedBatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BatchDeleteOperation<R extends DynamoRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private final BatchWriteItemRequest.Builder builder;
    private final List<K> keys;
    private boolean debug = false;

    public BatchDeleteOperation(Table<R, K> table) {
        this(table, null);
    }

    public BatchDeleteOperation(Table<R, K> table, DynamoDbClient client) {
        super(table);

        this.client = client;

        keys = new ArrayList<>();
        builder = BatchWriteItemRequest.builder();
    }

    public BatchDeleteOperation<R, K> debug() {
        debug = true;
        return this;
    }

    public BatchDeleteOperation<R, K> delete(K value) {

        keys.add(value);

        return this;
    }

    public BatchDeleteOperation<R, K> deleteAll(List<K> values) {

        keys.addAll(values);

        return this;
    }

    private WriteRequest deleteRequest(K key) {

        return WriteRequest.builder()
                .deleteRequest(DeleteRequest.builder()
                        .key(key)
                        .build())
                .build();
    }

    public BufferedBatchWriteItemRequest execute() {
        return execute(getClient());
    }

    public BufferedBatchWriteItemRequest execute(DynamoDbClient client) {

        if (keys.isEmpty()) throw new IllegalStateException("No items specified to delete");

        var requests = keys.stream()
                .map(this::deleteRequest)
                .collect(Collectors.toList());

        var writeMap = Map.of(getTable().getTableName(), requests);

        var request = builder.requestItems(writeMap)
                .build();

        if (debug) Logger.getLogger(BatchDeleteOperation.class.getName())
                .log(Level.INFO, request.toString());

        return new BufferedBatchWriteItemRequest(client.batchWriteItem(request));

    }
}
