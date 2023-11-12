package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Table;
import org.dooq.core.DynamoOperation;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PaginateOperation<R extends AbstractRecord<R>, K extends Key> extends DynamoOperation<R, K> {
    public PaginateOperation(Table<R, K> table) {
        super(table);
    }

    public PaginateOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public PaginateOperation<R, K> startingFrom(Function<KeyBuilder, KeyBuilder> key) {
        return this;
    }

    public PaginateOperation<R, K> limit(int size) {
        return this;
    }

    public PaginateOperation<R, K> limit(int size, int itemsPerPage) {
        return this;
    }

    public <T> List<T> fetchInto(Class<T> type) {
        return Collections.emptyList();
    }

}
