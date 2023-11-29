package org.dooq;


import org.dooq.api.Column;
import org.dooq.core.response.BatchGetMultiTableResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchOperation {
    private final List<GetOperation<?, ?>> operationList;
    private final DynamoDbClient client;
    private boolean consistent;

    public BatchOperation(DynamoDbClient client) {
        this.client = client;
        this.operationList = new ArrayList<>();
    }

    public BatchOperation add(GetOperation<?, ?> operation) {
        operationList.add(operation);
        return this;
    }

    public BatchOperation union(GetOperation<?, ?> operation) {
        operationList.add(operation);
        return this;
    }

    public BatchOperation consistent() {
        this.consistent = true;
        return this;
    }

    public BatchGetMultiTableResponse execute() {

        Map<String, KeysAndAttributes> map = new HashMap<>();

        var opMap = operationList.stream()
                .collect(Collectors.groupingBy(a -> a.getTable().getTableName()));

        for (Map.Entry<String, List<GetOperation<?, ?>>> entry : opMap.entrySet()) {

            var projection = entry.getValue().stream()
                    .flatMap(a -> a.getColumns().stream())
                    .map(Column::name)
                    .collect(Collectors.toSet());

            var keys = entry.getValue().stream()
                    .map(a -> a.build().key())
                    .toList();

            map.put(entry.getKey(), KeysAndAttributes.builder()
                    .keys(keys)
                    .attributesToGet(projection)
                    .consistentRead(consistent)
                    .build());
        }

        var response = client.batchGetItem(BatchGetItemRequest.builder()
                .requestItems(map)
                .build());

        return new BatchGetMultiTableResponse(response);
    }

}
