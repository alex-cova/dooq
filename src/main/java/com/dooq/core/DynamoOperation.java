package com.dooq.core;

import com.dooq.api.AbstractRecord;
import com.dooq.Key;
import com.dooq.api.Table;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoOperation<R extends AbstractRecord<R>, K extends Key> {

    private final Table<R, K> table;

    protected DynamoDbClient client;

    public final DynamoDbClient getClient() {
        return client;
    }

    public DynamoOperation(Table<R, K> table) {
        this.table = table;
    }

    public Table<R, K> getTable() {
        return table;
    }

    public boolean notEmpty(String value) {
        if (value == null) return false;

        return !value.isEmpty();
    }

}
