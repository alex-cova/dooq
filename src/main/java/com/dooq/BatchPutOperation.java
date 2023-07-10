package com.dooq;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Table;
import com.dooq.core.response.BufferedBatchWriteItemRequest;
import com.dooq.core.DynamoOperation;
import com.dooq.core.ItemParser;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BatchPutOperation<R extends AbstractRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private final BatchWriteItemRequest.Builder builder;
    private final List<R> items;
    private boolean debug = false;

    public BatchPutOperation(Table<R, K> table) {
        super(table);

        items = new ArrayList<>();
        builder = BatchWriteItemRequest.builder();
    }

    public BatchPutOperation(Table<R, K> table, DynamoDbClient client) {
        super(table);

        this.client = client;

        items = new ArrayList<>();
        builder = BatchWriteItemRequest.builder();
    }

    public BatchPutOperation<R, K> debug() {
        debug = true;
        return this;
    }

    public BatchPutOperation<R, K> putAll(Collection<R> collection) {
        items.addAll(collection);

        return this;
    }

    public BatchPutOperation<R, K> put(R value) {

        items.add(value);

        return this;
    }

    public BatchPutOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;

        return this;
    }

    private WriteRequest write(R value) {

        return WriteRequest.builder()
                .putRequest(PutRequest.builder()
                        .item(ItemParser.writeRecord(value).map())
                        .build())
                .build();
    }

    public BufferedBatchWriteItemRequest execute() {

        var writeMap = new HashMap<String, List<WriteRequest>>();

        writeMap.put(getTable().getTableName(), items
                .stream()
                .map(this::write)
                .collect(Collectors.toList()));

        var request = builder.requestItems(writeMap)
                .build();

        if (debug) Logger.getLogger(BatchPutOperation.class.getName())
                .log(Level.INFO, request.toString());

        var response = getClient()
                .batchWriteItem(request);

        return new BufferedBatchWriteItemRequest(response);

    }
}
