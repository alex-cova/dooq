package org.dooq;

import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.DynamoOperation;
import org.dooq.core.response.BufferedTransactWriteItemsResponse;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_TransactWriteItems.html
 *
 * @author alex
 */
public class Transaction<R extends DynamoRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private TransactWriteItemsRequest.Builder builder;
    private final List<DeleteOperation<R, K>> deleteOperationList = new ArrayList<>();
    private final List<PutOperation<R, K>> putOperationList = new ArrayList<>();
    private final List<UpdateOperation<R, K>> updateOperationList = new ArrayList<>();
    private boolean debug;
    private String clientRequestToken;

    public Transaction(Table<R, K> table) {
        super(table);

        builder = TransactWriteItemsRequest.builder();
    }

    public Transaction<R, K> debug() {
        debug = true;
        return this;
    }

    public Transaction<R, K> update(UpdateOperation<R, K> updateOperation) {
        this.updateOperationList.add(updateOperation);
        return this;
    }

    public Transaction<R, K> put(PutOperation<R, K> putOperation) {
        putOperationList.add(putOperation);
        return this;
    }

    public Transaction<R, K> delete(DeleteOperation<R, K> deleteOperation) {
        deleteOperationList.add(deleteOperation);
        return this;
    }

    /**
     * Providing a ClientRequestToken makes the call to TransactWriteItems idempotent,
     * meaning that multiple identical calls have the same effect as one single call.
     * <p>
     * Although multiple identical calls using the same client request token produce the same result on the server (no side effects),
     * the responses to the calls might not be the same. If the ReturnConsumedCapacity> parameter is set, then the initial
     * TransactWriteItems call returns the amount of write capacity units consumed in making the changes.
     * Subsequent TransactWriteItems calls with the same client token return the number of read capacity units consumed in reading the item.
     * <p>
     * A client request token is valid for 10 minutes after the first request that uses it is completed. After 10 minutes,
     * any request with the same client token is treated as a new request. Do not resubmit the same request with the same
     * client token for more than 10 minutes, or the result might not be idempotent.
     * <p>
     * If you submit a request with the same client token but a change in other parameters within the 10-minute idempotency window,
     * DynamoDB returns an IdempotentParameterMismatch exception.
     */
    public Transaction<R, K> setClientRequestToken(String clientRequestToken) {
        this.clientRequestToken = clientRequestToken;
        return this;
    }

    private TransactWriteItem buildDelete(@NotNull DeleteOperation<R, K> deleteOperation) {
        return TransactWriteItem
                .builder()
                .delete(deleteOperation.transact())
                .build();
    }

    private TransactWriteItem buildPut(@NotNull PutOperation<R, K> putOperation) {
        return TransactWriteItem
                .builder()
                .put(putOperation.transact())
                .build();
    }

    private TransactWriteItem buildUpdate(@NotNull UpdateOperation<R, K> updateOperation) {
        return TransactWriteItem
                .builder()
                .update(updateOperation.transact())
                .build();
    }

    public BufferedTransactWriteItemsResponse execute() {

        List<TransactWriteItem> operations = new ArrayList<>();

        var deletes = deleteOperationList.stream()
                .map(this::buildDelete).toList();

        var puts = putOperationList.stream()
                .map(this::buildPut).toList();

        var updates = updateOperationList
                .stream()
                .map(this::buildUpdate).toList();

        operations.addAll(deletes);
        operations.addAll(puts);
        operations.addAll(updates);

        if (operations.isEmpty()) return new BufferedTransactWriteItemsResponse(null);

        builder = builder
                .transactItems(operations);

        if (clientRequestToken != null) {
            builder = builder.clientRequestToken(clientRequestToken);
        }

        var request = builder.build();

        if (debug) Logger.getLogger(Transaction.class.getName())
                .log(Level.INFO, request.toString());

        TransactWriteItemsResponse transactWriteItemsResponse = getClient()
                .transactWriteItems(request);


        return new BufferedTransactWriteItemsResponse(transactWriteItemsResponse);
    }

}
