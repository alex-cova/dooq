package org.dooq.core.response;

import org.dooq.Key;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.ListResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;

public class BufferedQueryResponse<R extends DynamoRecord<R>, K extends Key> implements ListResponse<R, K> {

    private final QueryResponse response;
    private final Table<R, K> table;

    public BufferedQueryResponse(QueryResponse response, Table<R, K> table) {
        this.response = response;
        this.table = table;
    }

    public QueryResponse response() {
        return response;
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
        return !isEmpty();
    }
}
