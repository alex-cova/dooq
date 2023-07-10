package com.dooq.core.response;

import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;

public record BufferedTransactWriteItemsResponse(TransactWriteItemsResponse response) {

}
