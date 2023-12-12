package org.dooq.core;

import org.dooq.Key;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoOperation<R extends DynamoRecord<R>, K extends Key> {

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
