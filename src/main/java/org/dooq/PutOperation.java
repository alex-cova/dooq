package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.api.Table;
import org.dooq.core.exception.DynamoOperationException;
import org.dooq.core.AttributeWriter;
import org.dooq.core.Condition;
import org.dooq.core.DynamoOperation;
import org.dooq.core.ItemParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PutOperation<R extends AbstractRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private final HashMap<String, AttributeValue> data;
    private PutItemRequest.Builder builder;
    private Key key;
    private boolean debug;
    private Condition condition;

    public PutOperation(@NotNull Table<R, K> table) {
        super(table);

        this.data = new HashMap<>();
        this.builder = PutItemRequest.builder()
                .tableName(table.getTableName());
    }

    @Contract("_ -> new")
    public static <R extends AbstractRecord<R>, K extends Key> @NotNull PutOperation<R, K> into(Table<R, K> table) {
        return new PutOperation<>(table);
    }

    public PutOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public PutOperation<R, K> set(@NotNull Column<R, K> column, Object value) {

        if (value == null) return this;

        data.put(column.name(), AttributeWriter.parse(value));
        return this;
    }

    public PutOperation<R, K> set(@NotNull String column, Object value) {

        if (value == null) return this;

        data.put(column, AttributeWriter.parse(value));

        return this;
    }

    public PutOperation<R, K> value(@NotNull R object) {

        var result = ItemParser.writeRecord(object);

        if (result.key() != null) {
            key(result.key());
        }

        data.putAll(result.map());

        return this;
    }

    public PutOperation<R, K> key(Key key) {
        this.key = key;
        return this;
    }

    public PutOperation<R, K> condition(@NotNull Condition condition) {
        this.condition = condition;
        return this;
    }

    public PutOperation<R, K> debug() {
        this.debug = true;
        return this;
    }

    public PutItemResponse execute() {
        return execute(client);
    }

    public PutItemResponse execute(@NotNull DynamoDbClient client) {

        var operation = build();

        try {
            return client.putItem(operation);
        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }
    }

    /**
     * This method must be used for BatchWrite
     *
     * @return The put item request
     */
    public PutItemRequest build() {

        if (data.isEmpty()) {
            throw new IllegalArgumentException("Can't put item without properties");
        }

        if (key == null) {
            throw new IllegalStateException("No key specified");
        }

        if (condition != null) {
            builder = builder.conditionExpression(condition.getCommand());
        }

        var operation = builder.item(buildItem())
                .build();

        if (debug) {
            Logger.getLogger(PutOperation.class.getName())
                    .log(Level.INFO, operation.toString());
        }

        return operation;
    }

    private @NotNull Map<String, AttributeValue> buildItem() {
        var result = new HashMap<>(data);
        result.putAll(key);

        return result;
    }

    protected software.amazon.awssdk.services.dynamodb.model.Put transact() {
        var pre = software.amazon.awssdk.services.dynamodb.model.Put
                .builder()
                .tableName(getTable().getTableName())
                .item(buildItem());

        if (condition != null) {
            pre = pre.conditionExpression(condition.getCommand());
        }

        return pre.build();
    }

}
