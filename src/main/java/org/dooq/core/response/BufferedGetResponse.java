package org.dooq.core.response;

import org.dooq.Key;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.SingleResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.Map;

public class BufferedGetResponse<R extends DynamoRecord<R>, K extends Key> implements SingleResponse<R, K> {

    private final GetItemResponse response;
    private final Table<R, K> table;

    public BufferedGetResponse(GetItemResponse response, Table<R, K> table) {
        this.response = response;
        this.table = table;
    }

    public GetItemResponse response() {
        return response;
    }

    public Table<R, K> table() {
        return table;
    }

    @Override
    public boolean isEmpty() {
        return response.item().isEmpty();
    }

    @Override
    public boolean hasItems() {
        return !isEmpty();
    }

    public boolean hasItem() {
        return response.hasItem();
    }

    @Override
    public Map<String, AttributeValue> getItem() {
        return response.item();
    }

    @Override
    public Table<R, K> getTable() {
        return table;
    }
}
