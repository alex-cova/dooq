package com.dooq.core.response;

import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BufferedBatchWriteItemResponse {

    private final List<ConsumedCapacity> consumedCapacity;
    private final Map<String, List<WriteRequest>> unprocessedItems;

    public BufferedBatchWriteItemResponse(BatchWriteItemResponse response) {
        this();
        append(response);
    }

    public void append(BatchWriteItemResponse response) {
        consumedCapacity.addAll(response.consumedCapacity());

        if (response.hasUnprocessedItems()) {
            for (String s : response.unprocessedItems().keySet()) {
                unprocessedItems.putIfAbsent(s, new ArrayList<>());
            }

            for (Map.Entry<String, List<WriteRequest>> entry : response.unprocessedItems().entrySet()) {
                unprocessedItems.get(entry.getKey())
                        .addAll(entry.getValue());
            }
        }
    }

    public boolean hasUnprocessedItems() {
        return !unprocessedItems.isEmpty();
    }

    public BufferedBatchWriteItemResponse() {
        this.consumedCapacity = new ArrayList<>();
        unprocessedItems = new HashMap<>();
    }

    public List<ConsumedCapacity> consumedCapacity() {
        return consumedCapacity;
    }
}
