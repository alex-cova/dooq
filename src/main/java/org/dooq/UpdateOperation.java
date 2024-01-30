package org.dooq;


import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.*;
import org.dooq.core.exception.DynamoOperationException;
import org.dooq.core.response.BufferedUpdateResponse;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.expressions.AddExpression;
import org.dooq.expressions.ListAppendExpression;
import org.dooq.util.NullableValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UpdateOperation<R extends DynamoRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private final Map<String, AttributeValue> attributeValueMap;
    private UpdateItemRequest.Builder builder;
    private final List<Expression> setExpressions = new ArrayList<>();
    private final List<Expression> addExpressions = new ArrayList<>();
    private boolean debug;

    public UpdateOperation(@NotNull Table<R, K> table) {
        super(table);
        builder = UpdateItemRequest.builder()
                .tableName(table.getTableName());
        this.attributeValueMap = new HashMap<>();
    }

    protected UpdateOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    @Contract(value = "_ -> new", pure = true)
    public static <R extends DynamoRecord<R>, K extends Key> @NotNull UpdateOperation<R, K> table(Table<R, K> table) {
        return new UpdateOperation<>(table);
    }

    public List<Expression> getSetExpressions() {
        return setExpressions;
    }

    public UpdateItemRequest.Builder getBuilder() {
        return builder;
    }

    public Map<String, AttributeValue> getAttributeValueMap() {
        return attributeValueMap;
    }

    public boolean isDebug() {
        return debug;
    }

    public UpdateOperation<R, K> where(@NotNull ExpressionRenderer<R, K> key) {

        builder = builder.key(key.getValue().entrySet()
                .stream()
                .collect(Collectors.toMap(a -> a.getKey().name(), a -> AttributeWriter.parse(a.getValue()))));

        return this;
    }

    public UpdateOperation<R, K> key(K key) {
        builder = builder.key(key);
        return this;
    }

    public UpdateOperation<R, K> key(@NotNull Function<KeyBuilder, KeyBuilder> key) {
        builder = builder.key(key.apply(new KeyBuilder(getTable())).build());
        return this;
    }

    protected UpdateOperation<R, K> keyInternal(Key key) {
        builder = builder.key(key);
        return this;
    }


    public UpdateOperation<R, K> increment(@NotNull Column<R, K> column, BigDecimal value) {

        var param = column.param();

        setExpressions.add(new ComplexExpression(column, column.escapedName() + " = " + column.escapedName() + " + " + param));
        attributeValueMap.put(param, AttributeWriter.parse(value));

        return this;
    }

    public UpdateOperation<R, K> add(@NotNull Column<R, K> column, BigDecimal value) {

        var param = column.param();

        addExpressions.add(new AddExpression(column, value));
        attributeValueMap.put(param, AttributeWriter.parse(value));

        return this;
    }

    public UpdateOperation<R, K> subtract(@NotNull Column<R, K> column, BigDecimal value) {
        var param = column.param();

        setExpressions.add(new ComplexExpression(column, column.escapedName() + " = " + column.escapedName() + " - " + param));
        attributeValueMap.put(param, AttributeWriter.parse(value));

        return this;
    }


    @ApiStatus.Internal
    protected void setInternal(@NotNull Column<R, ?> column, Object optional) {

        var param = column.param();

        setExpressions.add(new UpdateExpression(column, "=", param));
        attributeValueMap.put(param, AttributeWriter.parse(optional));
    }

    public UpdateOperation<R, K> setNull(@NotNull Column<R, K> column) {
        var param = column.param();

        setExpressions.add(new UpdateExpression(column, "=", param));
        attributeValueMap.put(param, AttributeWriter.nil());

        return this;
    }

    /**
     * item = ddbjson.loads(updates_table.get_item(TableName=os.environ["SUMO_TABLE"], Key={"service_hash": { 'S': service_hash}})["Item"])
     * resp = updates_table.update_item(TableName=os.environ["SUMO_TABLE"],
     * Key={"service_hash": { 'S': service_hash} },
     * UpdateExpression='SET #fs = list_append(#fs, :fs), #urls = list_append(#urls, :urls)',
     * ExpressionAttributeNames={ "#fs": "full_sources", "#urls": "urls"}, ExpressionAttributeValues={
     * ":fs": { "L": [{ "S": source_data["full_source"]}] },
     * ":urls": { "L": [{"S": source_data["url"]}] }
     * })
     */
    public UpdateOperation<R, K> append(@NotNull Column<R, K> column, @Nullable Object value) {

        setExpressions.add(new ListAppendExpression<>(column));
        attributeValueMap.put(column.param(), AttributeWriter.parse(value));

        return this;
    }

    public UpdateOperation<R, K> set(@NotNull Column<R, K> column, @Nullable Object value) {
        set(column, value, "=");
        return this;
    }

    public UpdateOperation<R, K> decrement(@NotNull Column<R, K> column) {
        return decrement(column, 1);
    }

    public UpdateOperation<R, K> decrement(@NotNull Column<R, K> column, Number value) {

        var param = column.param();

        setExpressions.add(new ComplexExpression(column, column.escapedName() + " = " + column.escapedName() + " - " + param));
        attributeValueMap.put(param, AttributeWriter.parse(value));

        return this;
    }

    public UpdateOperation<R, K> increment(@NotNull Column<R, K> column) {
        return increment(column, 1);
    }

    public UpdateOperation<R, K> increment(@NotNull Column<R, K> column, Number value) {

        var param = column.param();

        setExpressions.add(new ComplexExpression(column, column.escapedName() + " = " + column.escapedName() + " + " + param));
        attributeValueMap.put(param, AttributeWriter.parse(value));

        return this;
    }

    public UpdateOperation<R, K> set(Object value, Column<R, K> column) {
        return set(column, value);
    }

    public UpdateOperation<R, K> set(@NotNull Column<R, K> column, @Nullable Object value, String command) {

        if (value == null) {
            return this;
        }

        if (value instanceof NullableValue<?> nullable) {
            if (nullable.isNull()) {
                return setNull(column);
            }
        }

        var param = column.param();

        setExpressions.add(new ComplexExpression(column, column.escapedName() + " " + command + " " + param));
        attributeValueMap.put(param, AttributeWriter.parse(value));

        return this;
    }


    public UpdateOperation<R, K> attributeValue(String key, Object value) {
        attributeValueMap.putAll(AttributeWriter.parsing(key, value));

        return this;
    }

    public UpdateOperation<R, K> condition(@NotNull Condition condition) {
        return this.condition(condition.getCommand());
    }

    public UpdateOperation<R, K> when(@NotNull Column<R, K> column, @Nullable Object value) {

        if (value == null) return this;

        builder = builder.conditionExpression(column.name() + " = :currval");
        attributeValueMap.put(":currval", AttributeWriter.parse(value));

        return this;
    }

    public UpdateOperation<R, K> condition(String condition) {

        builder = builder.conditionExpression(condition);

        return this;
    }

    public UpdateOperation<R, K> debug() {

        this.debug = true;

        return this;
    }

    public UpdateReturn<R, K> returning(ReturnValue value) {
        builder = builder.returnValues(value);
        return new UpdateReturn<>(this);
    }

    public BufferedUpdateResponse<R, K> execute() {
        return execute(client);
    }

    public BufferedUpdateResponse<R, K> execute(@NotNull DynamoDbClient client) {

        var operation = build();

        if (debug) {
            Logger.getLogger(UpdateOperation.class.getName())
                    .log(Level.INFO, operation.toString());
        }

        try {
            return new BufferedUpdateResponse<>(client.updateItem(operation), getTable());
        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }
    }

    private UpdateItemRequest build() {
        if (isEmpty()) {
            throw new IllegalStateException("Missing expressions");
        }

        builder = builder
                .updateExpression(buildUpdateExpression())
                .expressionAttributeNames(buildAttributeNamesMap())
                .expressionAttributeValues(attributeValueMap);

        return builder.build();
    }

    boolean isEmpty() {
        return setExpressions.isEmpty() && addExpressions.isEmpty();
    }

    public Map<String, String> buildAttributeNamesMap() {
        List<Expression> expressionList = new ArrayList<>(setExpressions.size() + addExpressions.size());

        expressionList.addAll(setExpressions);
        expressionList.addAll(addExpressions);

        return expressionList
                .stream()
                .collect(Collectors.toMap(a -> a.column().escapedName(), c -> c.column().name()));
    }

    public String buildUpdateExpression() {

        List<String> result = new ArrayList<>();

        if (!addExpressions.isEmpty()) {
            var addExpression = "ADD " + addExpressions.stream()
                    .map(Expression::render)
                    .collect(Collectors.joining(", "));

            result.add(addExpression);
        }

        if (!setExpressions.isEmpty()) {
            var setExpression = "SET " + setExpressions.stream()
                    .map(Expression::render)
                    .collect(Collectors.joining(", "));

            result.add(setExpression);
        }

        return String.join(", ", result);
    }

    protected software.amazon.awssdk.services.dynamodb.model.Update transact() {

        var operation = build();

        return software.amazon.awssdk.services.dynamodb.model.Update
                .builder()
                .tableName(getTable().getTableName())
                .key(operation.key())
                .conditionExpression(operation.conditionExpression())
                .expressionAttributeNames(operation.expressionAttributeNames())
                .expressionAttributeValues(operation.expressionAttributeValues())
                .updateExpression(operation.updateExpression())
                .build();
    }

}
