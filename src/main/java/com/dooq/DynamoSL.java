package com.dooq;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.core.response.BufferedBatchWriteItemResponse;
import com.dooq.core.response.BufferedGetResponse;
import com.dooq.engine.ParserCompiler;
import com.dooq.projection.Projection;
import com.dooq.util.ReflectionUtils;
import com.dooq.api.Table;
import com.dooq.core.ItemParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Java Oriented Object Query for DynamoDB
 * <p>
 * This library may look for RDMS only but still have all the capabilities
 * for a NoSQL database.
 *
 * @author Alex
 * @version 1.0
 */
public class DynamoSL {

    private final DynamoDbClient client;

    public DynamoSL(DynamoDbClient client) {
        this.client = client;
    }

    public DynamoDbClient asClient() {
        return client;
    }

    public <R extends AbstractRecord<R>, K extends Key> @NotNull UpdateOperation<R, K> update(Table<R, K> table) {
        return new UpdateOperation<>(table)
                .setClient(client);
    }

    public <R extends AbstractRecord<R>, K extends Key> @NotNull DeleteOperation<R, K> deleteFrom(Table<R, K> table) {
        return new DeleteOperation<>(table)
                .setClient(client);
    }

    public BufferedBatchWriteItemResponse delete(DeleteOperation<?, ?> operation, DeleteOperation<?, ?> @NotNull ... operations) {
        List<DeleteOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(operation);

        if (operations.length > 0) operationList.addAll(Arrays.asList(operations));

        Map<String, List<WriteRequest>> requestMap = new HashMap<>();

        var operationMap = operationList.stream()
                .collect(Collectors.groupingBy(c -> c.getTable().getTableName()));

        for (Map.Entry<String, List<DeleteOperation<?, ?>>> entry : operationMap.entrySet()) {
            var list = new ArrayList<WriteRequest>();
            requestMap.put(entry.getKey(), list);

            for (DeleteOperation<?, ?> op : entry.getValue()) {
                list.add(WriteRequest.builder()
                        .deleteRequest(r -> r.key(op.whichKey()))
                        .build());
            }
        }

        var response = client.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(requestMap)
                .build());

        return new BufferedBatchWriteItemResponse(response);
    }

    public DeleteItemResponse delete(Key key) {
        return client.deleteItem(a -> a.key(key));
    }

    public BufferedBatchWriteItemResponse delete(Key @NotNull ... keys) {
        return delete(Arrays.asList(keys));
    }

    public BufferedBatchWriteItemResponse delete(@NotNull Collection<Key> keys) {

        if (keys.size() == 0) return new BufferedBatchWriteItemResponse();

        Map<String, List<WriteRequest>> requestMap = new HashMap<>();

        for (Key key : keys) {
            if (key.getTable() == null || key.getTable().isBlank()) {
                throw new IllegalStateException("Table not specified for key " + key);
            }
        }

        var operationMap = keys.stream()
                .collect(Collectors.groupingBy(Key::getTable));

        for (Map.Entry<String, List<Key>> entry : operationMap.entrySet()) {
            var list = new ArrayList<WriteRequest>();
            requestMap.put(entry.getKey(), list);

            for (Key key : entry.getValue()) {
                list.add(WriteRequest.builder()
                        .deleteRequest(r -> r.key(key))
                        .build());
            }
        }

        var response = client.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(requestMap)
                .build());

        return new BufferedBatchWriteItemResponse(response);
    }

    public <R extends AbstractRecord<R>, K extends Key> GetOperation<R, K> selectFrom(Table<R, K> table) {
        return new GetOperation<>(table, Collections.emptyList())
                .setClient(client);
    }

    public <R extends AbstractRecord<R>, K extends Key> PreInsert<R, K> insertInto(Table<R, K> table) {
        return new PreInsert<>(table, client);
    }

    public <R extends AbstractRecord<R>, K extends Key> BatchDeleteOperation<R, K> batchDelete(Table<R, K> table) {
        return new BatchDeleteOperation<>(table, client);
    }

    public <R extends AbstractRecord<R>, K extends Key> BatchPutOperation<R, K> batchPut(Table<R, K> table) {
        return new BatchPutOperation<>(table, client);
    }

    public <R extends AbstractRecord<R>, K extends Key> BatchGetOperation<R, K> batchGet(Table<R, K> table) {
        return new BatchGetOperation<>(table, client);
    }

    public <R extends AbstractRecord<R>, K extends Key> TableIterator<R, K> iterate(Table<R, K> table) {
        return new TableIterator<>(table)
                .setClient(client);
    }

    public <R extends AbstractRecord<R>, K extends Key> Transaction<R, K> transaction(Table<R, K> table) {
        return new Transaction<>(table);
    }

    public boolean anyPresent(@NotNull BatchGetOperation<?, ?> batch) {
        return batch.strip()
                .execute()
                .anyPresent();
    }

    public boolean fetchExists(@NotNull BatchGetOperation<?, ?> batch) {
        return batch
                .strip()
                .execute()
                .allPresent();
    }

    public boolean fetchExists(@NotNull GetOperation<?, ?> get) {
        var request = get.strip();

        return !new BufferedGetResponse<>(client.getItem(request), get.getTable())
                .isEmpty();
    }

    public boolean fetchExists(@NotNull QueryOperation<?, ?> queryOperation) {
        return !queryOperation.limit(1)
                .fetch()
                .isEmpty();
    }

    public boolean fetchExists(@NotNull ScanOperation<?, ?> scanOperation) {

        return !scanOperation.limit(1)
                .execute(client)
                .isEmpty();
    }

    public <R extends AbstractRecord<R>, K extends Key> ScanOperation<R, K> scan(Table<R, K> table) {
        return new ScanOperation<>(table)
                .setClient(client);
    }

    public final <T, R extends AbstractRecord<R>, K extends Key> ProjectedGet<Projection.ProjectionResult1<T>, R, K>
    select(com.dooq.api.Field<T, R, K> column) {
        return ProjectedGet.of(column.table(), column)
                .setClient(client);
    }

    @SafeVarargs
    public final <R extends AbstractRecord<R>, K extends Key> PreGet<R, K> select(Column<R, K>... columns) {
        return PreGet.get(columns)
                .setClient(client);
    }

    public final <R extends AbstractRecord<R>, K extends Key> PreGet<R, K> select(List<Column<R, K>> columns) {
        return PreGet.get(columns)
                .setClient(client);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public <T extends AbstractRecord<T>> void update(@NotNull AbstractRecord<T> record) {
        Objects.requireNonNull(record.getRepresentation());

        AbstractRecord<?> original = ItemParser.readRecord(record.getRepresentation(), record.getClass());

        Map<String, ? extends Column<T, ?>> columnMap = record.getTable().getColumns()
                .stream()
                .collect(Collectors.toMap(Column::name, b -> b));

        var update = UpdateOperation.table(record.getTable())
                .keyInternal(record.getKey());

        for (Field field : record.getClass().getDeclaredFields()) {

            try {
                var a = field.get(record);
                var b = field.get(original);


                if (a instanceof Collection<?> cA && b instanceof Collection<?> cB) {

                    if (!cB.containsAll(cA)) {
                        update.setInternal(columnMap.get(field.getName()), a);
                    }
                } else {
                    if (!Objects.equals(a, b)) {
                        update.setInternal(columnMap.get(field.getName()), a);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        update.execute(client);
    }

    public <T extends AbstractRecord<T>> PutItemResponse store(@NotNull T object) {

        Objects.requireNonNull(object.getTable(), "You must specify the target table using dsl.newRecord(table)");

        return PutOperation.into(object.getTable())
                .value(object)
                .execute(client);

    }

    public <T extends AbstractRecord<T>> T newRecord(@NotNull Table<T, ?> table) {

        T value = ParserCompiler.getParser(table.getRecordType()).newInstance();

        value.setDsl(this);
        value.setTable(table);

        return value;
    }

    /**
     * @See https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_BatchWriteItem.html
     */
    public final <T extends AbstractRecord<T>> @Nullable BufferedBatchWriteItemResponse store(@NotNull List<? extends AbstractRecord<T>> records) {
        if (records.isEmpty()) return null;


        if (records.size() <= 25) {
            return new BufferedBatchWriteItemResponse(storeCollection(records));
        }

        BufferedBatchWriteItemResponse response = new BufferedBatchWriteItemResponse();

        for (int i = 0; i < records.size(); i += 25) {
            List<? extends AbstractRecord<T>> recordList = records.subList(i, Math.min(records.size(), i + 25));

            response.append(storeCollection(recordList));
        }

        return response;
    }

    private <T extends AbstractRecord<T>> BatchWriteItemResponse storeCollection(@NotNull Collection<? extends AbstractRecord<T>> records) {

        Map<String, List<WriteRequest>> requests = new HashMap<>();

        List<WriteRequest> writeRequests = new ArrayList<>();

        for (AbstractRecord<T> record : records) {

            requests.putIfAbsent(record.getTable().getTableName(), writeRequests);

            var result = ItemParser.write(record);

            var wr = WriteRequest.builder()
                    .putRequest(PutRequest.builder()
                            .item(result.map())
                            .build())
                    .build();

            writeRequests.add(wr);
        }

        return client.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(requests)
                .build());
    }
}
