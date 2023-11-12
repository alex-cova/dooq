package org.dooq;

import org.dooq.core.exception.DynamoOperationException;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Table;
import org.dooq.core.Condition;
import org.dooq.core.DynamoOperation;
import org.dooq.engine.ExpressionCompiler;
import org.dooq.engine.ExpressionRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;

import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteOperation<R extends AbstractRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private DeleteItemRequest.Builder builder;
    private ExpressionRenderer<R, K> expression;
    private Key key;
    private String conditionExpression;
    private boolean debug;


    public DeleteOperation(@NotNull Table<R, K> table) {
        super(table);

        builder = DeleteItemRequest.builder()
                .tableName(table.getTableName())
                .returnValues(ReturnValue.ALL_OLD);

    }

    public DeleteOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    @Contract("_ -> new")
    public static <R extends AbstractRecord<R>, K extends Key> @NotNull DeleteOperation<R, K> from(Table<R, K> table) {
        return new DeleteOperation<>(table);
    }

    public DeleteOperation<R, K> key(K key) {
        this.key = key;
        return this;
    }

    public DeleteOperation<R, K> key(@NotNull Function<KeyBuilder, KeyBuilder> key) {

        var kb = new KeyBuilder(getTable());

        this.key = key.apply(kb).build();

        return this;
    }

    public DeleteOperation<R, K> where(ExpressionRenderer<R, K> expression) {
        this.expression = expression;
        return this;
    }

    protected @Nullable Map<String, AttributeValue> renderKeyExpression() {
        return ExpressionCompiler.buildKey(getTable(), expression);
    }

    public DeleteOperation<R, K> condition(@NotNull Condition condition) {
        return this.condition(condition.getCommand());
    }

    public DeleteOperation<R, K> condition(String condition) {
        this.conditionExpression = condition;
        return this;
    }

    public DeleteOperation<R, K> debug() {
        this.debug = true;
        return this;
    }

    public boolean execute() {
        return execute(client);
    }

    protected Map<String, AttributeValue> whichKey() {
        if (key != null) return key;
        if (expression != null) return renderKeyExpression();

        throw new IllegalStateException("No key specified");
    }

    public boolean execute(@NotNull DynamoDbClient client) {

        if (conditionExpression != null && !conditionExpression.isBlank()) {
            builder = builder.conditionExpression(conditionExpression);
        }

        var operation = builder
                .key(whichKey())
                .build();

        if (debug) {
            Logger.getLogger(DeleteOperation.class.getName())
                    .log(Level.INFO, operation.toString());
        }

        try {
            return client.deleteItem(operation)
                    .hasAttributes();
        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }
    }

    public DeleteItemRequest build() {
        return builder.build();
    }

    protected software.amazon.awssdk.services.dynamodb.model.Delete transact() {
        var pre = software.amazon.awssdk.services.dynamodb.model.Delete.builder()
                .key(whichKey());

        if (conditionExpression != null && !conditionExpression.isBlank()) {
            pre = pre.conditionExpression(conditionExpression);
        }

        return pre.build();
    }

}
