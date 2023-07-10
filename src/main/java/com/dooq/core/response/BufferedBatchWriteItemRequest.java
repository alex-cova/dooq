package com.dooq.core.response;

import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;

public class BufferedBatchWriteItemRequest {

    private final BatchWriteItemResponse response;

    public BufferedBatchWriteItemRequest(BatchWriteItemResponse response) {
        this.response = response;
    }

    public BatchWriteItemResponse response() {
        return response;
    }

    public boolean isSuccess() {
        return response.unprocessedItems().isEmpty();
    }

}
