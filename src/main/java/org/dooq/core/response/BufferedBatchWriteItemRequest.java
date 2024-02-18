package org.dooq.core.response;

import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;

import java.util.List;

public class BufferedBatchWriteItemRequest {

    private final List<BatchWriteItemResponse> responses;

    public BufferedBatchWriteItemRequest(BatchWriteItemResponse response) {
        this.responses = List.of(response);
    }

    public BufferedBatchWriteItemRequest(List<BatchWriteItemResponse> responses) {
        this.responses = responses;
    }

    public BatchWriteItemResponse response() {
        return responses.get(0);
    }

    public boolean isSuccess() {
        return responses.stream().allMatch(a -> a.unprocessedItems().isEmpty());
    }

}
