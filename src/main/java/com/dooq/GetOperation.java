package com.dooq;

import com.dooq.core.*;
import com.dooq.core.exception.DynamoOperationException;
import com.dooq.core.response.BufferedGetResponse;
import com.dooq.util.AbstractColumn;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.api.Table;
import com.dooq.engine.ExpressionRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class GetOperation<R extends AbstractRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private GetItemRequest.Builder builder;
    private DynamoDbClient client;
    private Key key;
    private final List<Column<R, K>> columns;
    private boolean debug = false;

    public GetOperation(@NotNull Table<R, K> table, @NotNull List<Column<R, K>> columnList) {
        super(table);
        this.columns = new ArrayList<>();
        this.columns.addAll(columnList);

        builder = GetItemRequest.builder()
                .tableName(table.getTableName());
    }

    @ApiStatus.Experimental
    public PaginateOperation<R, K> until(ExpressionRenderer<?, ?> renderer) {
        return new PaginateOperation<>(getTable())
                .setClient(client);
    }

    @ApiStatus.Experimental
    public GetOperation<R, K> lateJoin(ExpressionRenderer<?, ?> renderer) {
        return this;
    }

    public GetOperation<R, K> join(Table<?, ?> table) {
        return this;
    }

    public GetOperation<R, K> join(GetOperation<?, ?> getOperation) {
        return this;
    }

    public GetOperation<R, K> join(QueryOperation<?, ?> queryOperation) {
        return this;
    }

    public ScanOperation<R, K> scan() {
        return new ScanOperation<>(getTable())
                .setClient(client);
    }

    public BatchGetOperation<R, K> batch() {
        return new BatchGetOperation<>(getTable())
                .setColumns(columns)
                .setClient(client);
    }

    public QueryOperation<R, K> onLocalIndex(Column<R, K> column) {
        return onIndex(column);
    }

    public QueryOperation<R, K> onSecondaryIndex(Column<R, K> column) {
        return onIndex(column);
    }

    public QueryOperation<R, K> onIndex(Column<R, K> column) {
        return new QueryOperation<>(getTable())
                .onIndex(column)
                .setClient(client);
    }

    public QueryOperation<R, K> onLocalIndex(String index) {
        return onIndex(index);
    }

    public QueryOperation<R, K> onSecondaryIndex(String index) {
        return onIndex(index);

    }

    public QueryOperation<R, K> onIndex(String index) {
        return new QueryOperation<>(getTable())
                .onIndex(new AbstractColumn<>(getTable(), index))
                .setClient(client);
    }

    protected GetItemRequest strip() {
        return GetItemRequest.builder()
                .tableName(getTable().getTableName())
                .key(key)
                .projectionExpression(key.getPartitionKeyName())
                .build();
    }

    public GetOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public GetOperation<R, K> consistentRead() {
        return consistentRead(true);
    }

    public GetOperation<R, K> consistentRead(boolean value) {
        builder = builder.consistentRead(value);
        return this;
    }

    public GetOperation<R, K> withKey(K key) {
        this.key = key;
        return this;
    }

    public GetOperation<R, K> withKey(@NotNull Function<KeyBuilder, KeyBuilder> key) {

        var kb = new KeyBuilder(getTable());

        this.key = key.apply(kb).build();

        return this;
    }

    protected GetOperation<R, K> withComplexKey(Key key) {
        this.key = key;

        return this;
    }

    public QueryOperation<R, K> where(ExpressionRenderer<R, K> renderer) {
        return new QueryOperation<>(getTable())
                .setClient(client)
                .select(columns)
                .where(renderer);
    }

    public @Nullable R fetch() {
        return execute()
                .into(getTable().getRecordType());
    }

    public @Nullable <A> A map(Function<R, A> function) {
        var r = fetch();

        if (r == null) return null;

        return function.apply(r);
    }

    public <T> T fetchInto(Class<T> clazz) {
        return execute()
                .into(clazz);
    }

    public GetOperation<R, K> debug() {
        debug = true;
        return this;
    }

    public SingleResponse<R, K> execute() {
        return execute(client);
    }

    public SingleResponse<R, K> execute(@NotNull DynamoDbClient client) {


        var pre = builder
                .key(Objects.requireNonNull(this.key, "Key is not present"));

        /*
         If no attribute names are provided, then all attributes will be returned.
         If any of the requested attributes are not found, they will not appear in the result.
         */
        if (columns.size() != getTable().getColumns().size()) {

            var escaped = ReservedWords.escapeProjection(columns);

            if (escaped != null) {
                pre = pre.projectionExpression(escaped.join());

                if (escaped.hasAttributeNames()) {
                    pre = pre.expressionAttributeNames(escaped.attributeNames());
                }
            }
        }

        var operation = pre.build();

        if (operation.key().size() == 1) {
            throw new DynamoOperationException("Partition and Sort Key must be present");
        }

        if (debug) {
            System.out.println(operation);
        }

        try {
            return new BufferedGetResponse<>(client.getItem(operation), getTable());
        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }
    }

}
