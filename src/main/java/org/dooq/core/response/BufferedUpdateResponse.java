package org.dooq.core.response;

import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Table;
import org.dooq.core.SingleResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Map;

public class BufferedUpdateResponse<R extends AbstractRecord<R>, K extends Key> implements SingleResponse<R, K> {

    private final UpdateItemResponse response;
    private final Table<R, K> table;

    public BufferedUpdateResponse(UpdateItemResponse response, Table<R, K> table) {
        this.response = response;
        this.table = table;
    }

    public UpdateItemResponse response() {
        return response;
    }

    public Table<R, K> table() {
        return table;
    }

    @Override
    public boolean isEmpty() {
        return response.attributes().isEmpty();
    }

    @Override
    public boolean hasItems() {
        return !isEmpty();
    }

    @Override
    public Map<String, AttributeValue> getItem() {
        return response.attributes();
    }

    @Override
    public Table<R, K> getTable() {
        return table;
    }
}
