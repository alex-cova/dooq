package org.dooq.core.response;

import org.dooq.Key;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.ListResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.List;
import java.util.Map;

public class BufferedScanResponse<R extends DynamoRecord<R>, K extends Key> implements ListResponse<R, K> {

    private final ScanResponse response;
    private final Table<R, K> table;

    public BufferedScanResponse(ScanResponse response, Table<R, K> table) {
        this.response = response;
        this.table = table;
    }


    public ScanResponse response() {
        return response;
    }

    public Table<R, K> table() {
        return table;
    }

    @Override
    public Table<R, K> getTable() {
        return table;
    }

    @Override
    public Map<String, AttributeValue> lastEvaluatedKey() {
        return response.lastEvaluatedKey();
    }

    @Override
    public List<Map<String, AttributeValue>> getItems() {
        return response.items();
    }

    @Override
    public boolean isEmpty() {
        return response.items().isEmpty();
    }

    @Override
    public boolean hasItems() {
        return response.hasItems();
    }
}
