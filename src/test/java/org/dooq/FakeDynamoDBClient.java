package org.dooq;

import org.junit.jupiter.api.Assertions;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@SuppressWarnings("all")
public class FakeDynamoDBClient implements DynamoDbClient {

    private Object lastRequest;

    @Override
    public String serviceName() {
        return "fake";
    }

    @Override
    public void close() {

    }

    public <T> T getLastRequest() {
        return (T) lastRequest;
    }

    public QueryRequest getQueryRequest() {
        return (QueryRequest) lastRequest;
    }

    public GetItemRequest getGetItemRequest() {
        return (GetItemRequest) lastRequest;
    }

    public void assertLastQueryRequest() {

        if (lastRequest == null) {
            Assertions.fail("Last request is null");
        }

        if (lastRequest instanceof QueryRequest) {
            return;
        }

        Assertions.fail("Last request was " + lastRequest.getClass().getSimpleName());
    }

    public void assertLastGetItemRequest() {

        if (lastRequest == null) {
            Assertions.fail("Last request is null");
        }

        if (lastRequest instanceof GetItemRequest) {
            return;
        }

        Assertions.fail("Last request was " + lastRequest.getClass().getSimpleName());
    }

    @Override
    public QueryResponse query(QueryRequest request) throws AwsServiceException, SdkClientException {
        this.lastRequest = request;

        printRequest(request);

        return QueryResponse.builder().build();
    }

    @Override
    public GetItemResponse getItem(GetItemRequest request) throws AwsServiceException, SdkClientException {
        this.lastRequest = request;

        printRequest(request);

        return GetItemResponse.builder().build();
    }

    @Override
    public DeleteItemResponse deleteItem(DeleteItemRequest request) throws AwsServiceException, SdkClientException {
        this.lastRequest = request;

        printRequest(request);

        return DeleteItemResponse.builder().build();
    }

    public void printRequest(Object value) {
        var result = value.toString()
                .replace(", FilterExpression", ",\n\tFilterExpression")
                .replace(", ProjectionExpression", ",\n\tProjectionExpression")
                .replace(", KeyConditionExpression", ",\n\tKeyConditionExpression")
                .replace(", ExpressionAttributeNames", ", \n\tExpressionAttributeNames")
                .replace(", ExpressionAttributeValues", ", \n\tExpressionAttributeValues");

        System.out.println(result);
    }
}
