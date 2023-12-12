package org.dooq;

import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.*;
import org.dooq.core.exception.DynamoOperationException;
import org.dooq.core.response.BufferedQueryResponse;
import org.dooq.engine.ExpressionCompiler;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.parser.ObjectParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryOperation<R extends DynamoRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private QueryRequest.Builder builder;
    private final List<ExpressionRenderer<R, K>> expressionList;
    private final List<Column<R, K>> attributesToGet;
    private Column<R, K> index;
    private boolean debug;

    public QueryOperation(@NotNull Table<R, K> table) {
        super(table);
        this.expressionList = new ArrayList<>();
        this.attributesToGet = new ArrayList<>();

        builder = QueryRequest.builder()
                .tableName(table.getTableName());

    }

    public QueryOperation<R, K> reversed(boolean reversed) {
        builder = builder.scanIndexForward(!reversed);
        return this;
    }

    public QueryOperation<R, K> where(ExpressionRenderer<R, K> renderer) {
        expressionList.add(renderer);
        return this;
    }

    public QueryOperation<R, K> consistentRead() {
        builder = builder.consistentRead(true);
        return this;
    }

    public QueryOperation<R, K> limit(int count) {
        builder = builder.limit(count);

        return this;
    }

    /**
     * Alias for exclusiveStartKey
     */
    public QueryOperation<R, K> startingFrom(Key key) {
        return exclusiveStartKey(key);
    }

    public QueryOperation<R, K> startingFrom(@NotNull Function<KeyBuilder, KeyBuilder> key) {

        exclusiveStartKey(key.apply(new KeyBuilder(getTable())).build());

        return this;
    }

    public QueryOperation<R, K> exclusiveStartKey(@Nullable Key key) {

        if (key == null) return this;

        builder = builder.exclusiveStartKey(key);

        return this;
    }

    QueryOperation<R, K> exclusiveStartKey(@Nullable LastEvaluatedKey key) {

        if (key == null) return this;

        builder = builder.exclusiveStartKey(key.key());

        return this;
    }

    public QueryOperation<R, K> select(List<Column<R, K>> columns) {
        attributesToGet.clear();
        attributesToGet.addAll(columns);

        return this;
    }


    public QueryOperation<R, K> onLocalIndex(@NotNull Column<R, K> column) {
        return onIndex(column);
    }

    public QueryOperation<R, K> onSecondaryIndex(@NotNull Column<R, K> column) {
        return onIndex(column);
    }


    public QueryOperation<R, K> onIndex(@NotNull Column<R, K> column) {
        this.index = column;

        return this;
    }

    public QueryOperation<R, K> debug() {
        this.debug = true;
        return this;
    }

    public QueryOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public QueryOperation<R, K> selectAll() {

        //no attributes to get = all attributes to fetch
        attributesToGet.clear();

        return this;
    }

    public Stream<R> stream() {
        var items = fetch();

        if (items.isEmpty()) return Stream.empty();

        return items.stream();
    }

    public @NotNull List<R> fetch() {
        return execute(client)
                .into(getTable().getRecordType());
    }

    public <T> @NotNull List<T> fetch(@NotNull ObjectParser<T> parser) {

        Objects.requireNonNull(parser, "Parser cannot be null");

        return execute(client)
                .into(parser);
    }

    public <T> @Nullable T fetchOne(@NotNull ObjectParser<T> parser) {

        Objects.requireNonNull(parser, "Parser cannot be null");

        return limit(1)
                .execute(client)
                .one(parser);
    }

    public @Nullable R fetchOne() {
        return limit(1)
                .execute(client)
                .one();
    }

    public <A> List<A> map(Function<R, A> function) {
        var result = fetch();

        if (result.isEmpty()) return Collections.emptyList();

        return result
                .stream()
                .map(function)
                .collect(Collectors.toList());
    }

    public <T> List<T> fetchInto(Class<T> type) {
        return execute(client)
                .into(type);
    }

    public ListResponse<R, K> execute() {
        return execute(client);
    }

    public ListResponse<R, K> execute(@NotNull DynamoDbClient client) {

        if (expressionList.isEmpty()) {
            if (!attributesToGet.isEmpty()) {

                var escaped = ReservedWords.escapeProjection(attributesToGet);

                if (escaped != null) {

                    builder = builder.expressionAttributeNames(escaped.attributeNames());

                    if (escaped.hasResults()) {
                        builder = builder.projectionExpression(escaped.join());
                    }
                }
            }
        } else {

            var compiled = ExpressionCompiler.compile(getTable(), expressionList, index);

            if (compiled.isSimpleGet()) {
                SingleResponse<R, K> res = PreGet.get(attributesToGet)
                        .from(getTable())
                        .withComplexKey(compiled.getComputedKey())
                        .execute(getClient());

                return SingleResponse.asListResponse(res);
            }


            builder = builder
                    .keyConditionExpression(compiled.keyCondition())
                    .expressionAttributeValues(AttributeWriter.parseMap(compiled.getExpressionAttributeValues()));

            var escaped = ReservedWords.escapeProjection(attributesToGet);

            if (escaped != null) {

                builder.expressionAttributeNames(escaped.merge(compiled.attributeNames()));

                if (escaped.hasResults()) {
                    builder = builder.projectionExpression(escaped.join());
                }
            } else {
                builder.expressionAttributeNames(compiled.attributeNames());
            }

            if (index != null) {
                builder = builder.indexName(index.name());
            }

            if (notEmpty(compiled.getFilterExpression())) {
                builder = builder.filterExpression(compiled.getFilterExpression());
            }
        }

        var operation = builder
                .build();

        if (debug) {
            Logger.getLogger(QueryOperation.class.getName())
                    .log(Level.INFO, operation.toString());
        }

        try {
            return new BufferedQueryResponse<>(client.query(operation), getTable());
        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }
    }
}
