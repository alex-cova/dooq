package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.core.exception.DynamoOperationException;
import org.dooq.core.response.BufferedUpdateResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class UpdateReturn<R extends AbstractRecord<R>, K extends Key> {
    private final UpdateOperation<R, K> updateOperation;

    public UpdateReturn(UpdateOperation<R, K> updateOperation) {
        this.updateOperation = updateOperation;
    }

    public @NotNull BufferedUpdateResponse<R, K> execute() {
        return execute(updateOperation.getClient());
    }

    @Contract("_ -> new")
    public @NotNull BufferedUpdateResponse<R, K> execute(@NotNull DynamoDbClient client) {

        if (updateOperation.isEmpty()) {
            throw new IllegalStateException("Missing expressions");
        }


        var attributeNames = updateOperation.getSetExpressions()
                .stream()
                .collect(Collectors.toMap(a -> a.column().escapedName(), c -> c.column().name()));

        var operation = updateOperation.getBuilder()
                .updateExpression(updateOperation.buildUpdateExpression())
                .expressionAttributeValues(updateOperation.getAttributeValueMap())
                .expressionAttributeNames(attributeNames)
                .conditionExpression(updateOperation.buildUpdateExpression())
                .build();

        if (update().isDebug()) {
            Logger.getLogger(UpdateReturn.class.getName())
                    .log(Level.INFO, operation.toString());
        }

        try {
            return new BufferedUpdateResponse<>(client.updateItem(operation), updateOperation.getTable());
        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }
    }

    public UpdateOperation<R, K> update() {
        return updateOperation;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UpdateReturn) obj;
        return Objects.equals(this.updateOperation, that.updateOperation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updateOperation);
    }

    @Override
    public String toString() {
        return "UpdateReturn[" +
                "update=" + updateOperation + ']';
    }

}
