package com.dooq.core.schema;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.api.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public class TableBuilder<R extends AbstractRecord<R>, K extends Key> {

    private final Table<R, K> table;
    private final CreateTableRequest.Builder builder;
    private final List<LocalIndex> localIndexList;
    private final List<GlobalIndex> globalIndexList;
    private boolean debug;

    static final class LocalIndex implements Index {
        private final LocalSecondaryIndex index;
        private final Column<?, ?> sortKey;
        private final Column<?, ?>[] projections;

        LocalIndex(LocalSecondaryIndex index, Column<?, ?> sortKey, Column<?, ?>... projections) {
            this.index = index;
            this.sortKey = sortKey;
            this.projections = projections;
        }

        public LocalIndex(LocalSecondaryIndex index, Column<?, ?> sortKey) {
            this(index, sortKey, new Column[0]);
        }

        @Override
        public @Nullable Column<?, ?> partitionKey() {
            return null;
        }

        public LocalSecondaryIndex index() {
            return index;
        }

        public Column<?, ?> sortKey() {
            return sortKey;
        }

        public Column<?, ?>[] projections() {
            return projections;
        }


    }

    static final class GlobalIndex implements Index {
        private final GlobalSecondaryIndex index;
        private final Column<?, ?> partitionKey;
        private final Column<?, ?> sortKey;
        private final Column<?, ?>[] projections;

        GlobalIndex(GlobalSecondaryIndex index, Column<?, ?> partitionKey, Column<?, ?> sortKey,
                    Column<?, ?>... projections) {
            this.index = index;
            this.partitionKey = partitionKey;
            this.sortKey = sortKey;
            this.projections = projections;
        }

        public GlobalIndex(GlobalSecondaryIndex index, Column<?, ?> partitionKey, Column<?, ?> sortKey) {
            this(index, partitionKey, sortKey, new Column[0]);
        }

        public GlobalSecondaryIndex index() {
            return index;
        }

        public Column<?, ?> partitionKey() {
            return partitionKey;
        }

        public Column<?, ?> sortKey() {
            return sortKey;
        }

        public Column<?, ?>[] projections() {
            return projections;
        }
    }

    interface Index {
        Column<?, ?> sortKey();

        Column<?, ?> partitionKey();

        Column<?, ?>[] projections();

        default List<Column<?, ?>> getColumns() {
            var list = new ArrayList<Column<?, ?>>();

            if (partitionKey() != null) {
                list.add(partitionKey());
            }

            list.add(sortKey());


            if (projections() != null && projections().length > 0) {
                list.addAll(Arrays.asList(projections()));
            }

            return list;
        }
    }

    public Column<R, K> getPartitionKey() {
        return table.getPartitionColumn();
    }

    public Column<R, K> getSortKey() {
        return table.getSortColumn();
    }

    public TableBuilder<R, K> debug() {
        debug = true;

        return this;
    }

    public static <R extends AbstractRecord<R>, K extends Key> TableBuilder<R, K> of(Table<R, K> table) {
        return new TableBuilder<>(table);
    }

    public TableBuilder(@NotNull Table<R, K> table) {
        this.table = table;

        this.localIndexList = new ArrayList<>();
        this.globalIndexList = new ArrayList<>();

        builder = CreateTableRequest.builder()
                .tableName(table.getTableName())
                .keySchema(keySchema());
    }

    @SafeVarargs
    public final TableBuilder<R, K> withOnlyKeysLocalIndex(Column<R, K> @NotNull ... columns) {
        for (Column<R, K> column : columns) {
            withOnlyKeysLocalIndex(column);
        }

        return this;
    }

    @SafeVarargs
    public final TableBuilder<R, K> withLocalIndex(Column<R, K> column, Column<R, K> @NotNull ... columns) {

        var names = Arrays.stream(columns)
                .map(Column::name)
                .collect(Collectors.toList());

        LocalSecondaryIndex localSecondaryIndex = localIndex(column, keySchema(column), names);

        localIndexList.add(new LocalIndex(localSecondaryIndex, column, columns));

        return this;
    }

    public TableBuilder<R, K> withOnlyKeysLocalIndex(Column<R, K> column) {
        localIndexList.add(new LocalIndex(onlyKeys(column), column));
        return this;
    }

    public TableBuilder<R, K> withGlobalIndex(Column<R, K> column) {
        globalIndexList.add(new GlobalIndex(globalSecondaryIndexAll(column, keySchema(column)), getPartitionKey(), column));
        return this;
    }

    public TableBuilder<R, K> withGlobalIndex(Column<R, K> partitionKey, Column<R, K> sortKey) {
        globalIndexList.add(new GlobalIndex(globalSecondaryIndexAll(sortKey, keySchema(partitionKey, sortKey)), partitionKey, sortKey));
        return this;
    }

    @SafeVarargs
    public final TableBuilder<R, K> withGlobalIndex(Column<R, K> column, Column<R, K>... projections) {
        globalIndexList.add(new GlobalIndex(globalSecondaryIndex(column, keySchema(column), projections), getPartitionKey(), column));
        return this;
    }

    public final TableBuilder<R, K> withGlobalIndex(Column<R, K> partition, Column<R, K> sort, List<Column<R, K>> projections) {
        globalIndexList.add(new GlobalIndex(globalSecondaryIndex(partition, keySchema(partition, sort), projections), partition, sort));
        return this;
    }

    @SafeVarargs
    public final TableBuilder<R, K> withGlobalIndex(String indexName, Column<R, K> sortColumn, Column<R, K>... projections) {
        globalIndexList.add(new GlobalIndex(globalSecondaryIndex(indexName, keySchema(sortColumn), Arrays.stream(projections)
                .map(Column::name)
                .collect(Collectors.toList())), getPartitionKey(), sortColumn));
        return this;
    }

    public final TableBuilder<R, K> withGlobalIndex(String indexName, Column<R, K> partitionColumn, Column<R, K> sortColumn) {
        globalIndexList
                .add(new GlobalIndex(globalSecondaryIndexOnlyKeys(indexName, keySchema(partitionColumn, sortColumn)), partitionColumn, sortColumn));

        return this;
    }

    private LocalSecondaryIndex onlyKeys(Column<R, K> column) {
        return onlyKeys(column, List.of(key(table.getPartitionColumn(), KeyType.HASH), key(column, KeyType.RANGE)));
    }

    private LocalSecondaryIndex onlyKeys(@NotNull Column<R, K> column, List<KeySchemaElement> keys) {
        return LocalSecondaryIndex.builder()
                .keySchema(keys)
                .indexName(column.name())
                .projection(Projection.builder()
                        .projectionType(ProjectionType.KEYS_ONLY)
                        .build())
                .build();
    }

    private LocalSecondaryIndex localIndex(@NotNull Column<R, K> column, List<KeySchemaElement> keys, List<String> attributes) {
        return LocalSecondaryIndex.builder()
                .keySchema(keys)
                .indexName(column.name())
                .projection(Projection.builder()
                        .projectionType(ProjectionType.INCLUDE)
                        .nonKeyAttributes(attributes)
                        .build())
                .build();
    }

    private GlobalSecondaryIndex globalSecondaryIndexAll(@NotNull Column<R, K> column, List<KeySchemaElement> keys) {
        return globalSecondaryIndexAll(column.name(), keys);
    }

    private GlobalSecondaryIndex globalSecondaryIndexAll(@NotNull String indexName, List<KeySchemaElement> keys) {
        return GlobalSecondaryIndex.builder()
                .keySchema(keys)
                .indexName(indexName)
                .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                .build();
    }

    @SafeVarargs
    private GlobalSecondaryIndex globalSecondaryIndex(@NotNull Column<R, K> column, List<KeySchemaElement> keys, Column<R, K>... projections) {
        var projectionList = Arrays.stream(projections)
                .map(Column::name)
                .collect(Collectors.toList());

        return globalSecondaryIndex(column.name(), keys, projectionList);
    }

    private GlobalSecondaryIndex globalSecondaryIndex(@NotNull Column<R, K> column, List<KeySchemaElement> keys, @NotNull List<Column<R, K>> projections) {
        var projectionList = projections.stream()
                .map(Column::name)
                .collect(Collectors.toList());

        return globalSecondaryIndex(column.name(), keys, projectionList);
    }

    private GlobalSecondaryIndex globalSecondaryIndex(@NotNull String indexName, List<KeySchemaElement> keys, List<String> projections) {
        return GlobalSecondaryIndex.builder()
                .keySchema(keys)
                .indexName(indexName)
                .projection(Projection.builder()
                        .projectionType(ProjectionType.INCLUDE)
                        .nonKeyAttributes(projections)
                        .build())
                .build();
    }

    private GlobalSecondaryIndex globalSecondaryIndexOnlyKeys(@NotNull String indexName, List<KeySchemaElement> keys) {
        return GlobalSecondaryIndex.builder()
                .keySchema(keys)
                .indexName(indexName)
                .projection(Projection.builder()
                        .projectionType(ProjectionType.KEYS_ONLY)
                        .build())
                .build();
    }

    public void create(@NotNull DynamoDbClient client) {

        var built = build();

        if (debug) {
            System.out.println(built);
        }

        client.createTable(built);
    }

    public CreateTableRequest build() {

        List<Column<?, ?>> definitions = new ArrayList<>();

        var ld = localIndexList.stream()
                .flatMap(a -> a.getColumns().stream()).toList();

        var gd = globalIndexList.stream()
                .flatMap(a -> a.getColumns().stream()).toList();

        definitions.addAll(ld);
        definitions.addAll(gd);
        definitions.add(table.getPartitionColumn());
        definitions.add(table.getSortColumn());

        definitions = definitions.stream()
                .distinct()
                .collect(Collectors.toList());

        if (!localIndexList.isEmpty()) {
            builder.localSecondaryIndexes(localIndexList
                    .stream()
                    .map(LocalIndex::index)
                    .collect(Collectors.toList()));
        }

        if (!globalIndexList.isEmpty()) {
            builder.globalSecondaryIndexes(globalIndexList.stream()
                    .map(GlobalIndex::index)
                    .collect(Collectors.toList()));
        }

        return builder.attributeDefinitions(definitions.stream()
                        .map(this::column)
                        .collect(Collectors.toList()))
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .provisionedThroughput(defaultProvisionedThroughput())
                .tags(local())
                .build();
    }

    private Tag local() {
        return Tag.builder()
                .key("table")
                .value(table.getTableName())
                .build();
    }

    private ProvisionedThroughput defaultProvisionedThroughput() {
        return ProvisionedThroughput.builder()
                .readCapacityUnits(0L)
                .writeCapacityUnits(0L)
                .build();
    }

    private @NotNull @Unmodifiable List<KeySchemaElement> keySchema() {
        return List.of(key(table.getPartitionColumn(), KeyType.HASH),
                key(table.getSortColumn(), KeyType.RANGE));
    }

    private @NotNull @Unmodifiable List<KeySchemaElement> keySchema(Column<R, K> column) {
        return List.of(key(table.getPartitionColumn(), KeyType.HASH),
                key(column, KeyType.RANGE));
    }

    private @NotNull @Unmodifiable List<KeySchemaElement> keySchema(Column<R, K> partition, Column<R, K> sort) {
        return List.of(key(partition, KeyType.HASH),
                key(sort, KeyType.RANGE));
    }

    private KeySchemaElement key(@NotNull Column<R, K> column, KeyType type) {
        return KeySchemaElement.builder()
                .attributeName(column.name())
                .keyType(type)
                .build();
    }

    private AttributeDefinition column(@NotNull Column<?, ?> column) {
        return column(column.name(), getType(column));
    }

    private AttributeDefinition column(String name, ScalarAttributeType type) {
        return AttributeDefinition.builder()
                .attributeName(name)
                .attributeType(type)
                .build();
    }

    private ScalarAttributeType getType(@NotNull Column<?, ?> column) {
        try {
            var field = table.getRecordType()
                    .getDeclaredField(column.name());

            if (isNumber(field.getType())) {
                return ScalarAttributeType.N;
            }

            return ScalarAttributeType.S;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Not found field " + column.name() + " on record class " + table
                    .getRecordType().getSimpleName(), e);
        }

    }

    private boolean isNumber(Class<?> clazz) {
        if (clazz == int.class) return true;
        if (clazz == long.class) return true;
        if (clazz == float.class) return true;
        if (clazz == double.class) return true;
        if (clazz == short.class) return true;

        return clazz.getSuperclass() == Number.class;
    }
}

