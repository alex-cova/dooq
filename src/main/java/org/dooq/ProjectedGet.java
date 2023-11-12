package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.api.FieldType;
import org.dooq.api.Table;
import org.dooq.core.ItemParser;
import org.dooq.core.exception.DynamoOperationException;
import org.dooq.core.ReservedWords;
import org.dooq.projection.Projection;
import org.dooq.engine.ExpressionRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Experimental
@SuppressWarnings("rawtypes")
public class ProjectedGet<T, R extends AbstractRecord<R>, K extends Key> {
    private DynamoDbClient client;
    private final Table<R, K> table;
    private final Class<T> type;
    private GetItemRequest.Builder builder;
    private final List<FieldType> columns;
    private Key key;
    private boolean debug = false;

    public ProjectedGet(@NotNull Table<R, K> table, Class<T> type, List<FieldType> columns) {
        this.table = table;
        this.type = type;
        this.columns = columns;
        builder = GetItemRequest.builder()
                .tableName(table.getTableName());

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, R extends AbstractRecord<R>, K extends Key> ProjectedGet<Projection.ProjectionResult1<T>, R, K>
    of(Table<R, K> table, FieldType<T> field) {
        return (ProjectedGet) new ProjectedGet<>(table, Projection.ProjectionResult1.class, List.of(field));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <f1, f2, R extends AbstractRecord<R>, K extends Key> ProjectedGet<Projection.ProjectionResult2<f1, f2>, R, K>
    of(Table<?, K> table, FieldType<f1> f1, FieldType<f2> f2) {
        return (ProjectedGet) new ProjectedGet<>(table, Projection.ProjectionResult1.class, List.of(f1, f2));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <f1, f2, f3, R extends AbstractRecord<R>, K extends Key> ProjectedGet<Projection.ProjectionResult3<f1, f2, f3>, R, K>
    of(Table<?, K> table, FieldType<f1> f1, FieldType<f2> f2, FieldType<f3> f3) {
        return (ProjectedGet) new ProjectedGet<>(table, Projection.ProjectionResult1.class, List.of(f1, f2, f3));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <f1, f2, f3, f4, R extends AbstractRecord<R>, K extends Key> ProjectedGet<Projection.ProjectionResult4<f1, f2, f3, f4>, R, K>
    of(Table<?, K> table, FieldType<f1> f1, FieldType<f2> f2, FieldType<f3> f3, FieldType<f4> f4) {
        return (ProjectedGet) new ProjectedGet<>(table, Projection.ProjectionResult1.class, List.of(f1, f2, f3, f4));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <f1, f2, f3, f4, f5, R extends AbstractRecord<R>, K extends Key> ProjectedGet<Projection.ProjectionResult5<f1, f2, f3, f4, f5>, R, K>
    of(Table<?, K> table, FieldType<f1> f1, FieldType<f2> f2, FieldType<f3> f3, FieldType<f4> f4, FieldType<f5> f5) {
        return (ProjectedGet) new ProjectedGet<>(table, Projection.ProjectionResult1.class, List.of(f1, f2, f3, f4, f5));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <f1, f2, f3, f4, f5, f6, R extends AbstractRecord<R>, K extends Key> ProjectedGet<Projection.ProjectionResult6<f1, f2, f3, f4, f5, f6>, R, K>
    of(Table<?, K> table, FieldType<f1> f1, FieldType<f2> f2, FieldType<f3> f3, FieldType<f4> f4, FieldType<f5> f5, FieldType<f6> f6) {
        return (ProjectedGet) new ProjectedGet<>(table, Projection.ProjectionResult1.class, List.of(f1, f2, f3, f4, f5, f6));
    }

    public QueryOperation<R, K> where(ExpressionRenderer<R, K> renderer) {
        return new QueryOperation<>(table)
                .where(renderer)
                .setClient(client);
    }

    public ProjectedGet<T, R, K> from(@NotNull Table<?, K> table) {
        if (table.getTableName().equals(this.table.getTableName())) {
            return this;
        }

        builder.tableName(table.getTableName());

        return this;
    }

    public ProjectedGet<T, R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public ProjectedGet<T, R, K> withKey(K key) {
        this.key = key;
        return this;
    }

    protected ProjectedGet<T, R, K> withComplexKey(Key key) {
        this.key = key;

        return this;
    }

    public ProjectedGet<T, R, K> consistentRead(boolean value) {
        builder = builder.consistentRead(value);
        return this;
    }

    public ProjectedGet<T, R, K> debug() {
        this.debug = true;
        return this;
    }

    public T execute() {
        return execute(client);
    }

    public T execute(@NotNull DynamoDbClient client) {

        var pre = builder
                .key(Objects.requireNonNull(this.key, "Key is not present"));

        var escaped = ReservedWords.escape(columns
                .stream()
                .map(FieldType::name)
                .toList());

        if (escaped != null) {
            pre = pre.projectionExpression(escaped.join());

            if (escaped.hasAttributeNames()) {
                pre = pre.expressionAttributeNames(escaped.attributeNames());
            }
        }

        var operation = pre.build();

        if (operation.key().size() == 1) {
            throw new DynamoOperationException("Partition and Sort Key must be present");
        }

        if (debug) {
            Logger.getLogger(ProjectedGet.class.getName())
                    .log(Level.INFO, operation.toString());
        }

        try {

            var response = client.getItem(operation);

            if (response.hasItem()) {
                return ItemParser.readRecord(response.item(), type);
            }

        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }

        return null;
    }
}
