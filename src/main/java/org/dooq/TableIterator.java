package org.dooq;

import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.DynamoOperation;
import org.dooq.core.LastEvaluatedKey;
import org.dooq.core.ListResponse;
import org.dooq.engine.ExpressionRenderer;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.*;

/**
 * Iterates over the result set of a query on the content of a partition.
 *
 * @author alex
 */
public class TableIterator<R extends DynamoRecord<R>, K extends Key> extends DynamoOperation<R, K> implements Iterable<R>, Iterator<R> {

    private final QueryOperation<R, K> queryOperation;
    private List<R> result = Collections.emptyList();
    private LastEvaluatedKey lastKey;
    private int chunkSize = 50;
    private final List<Column<R, K>> columns = new ArrayList<>();
    private boolean finished;

    public TableIterator(Table<R, K> table) {
        super(table);
        this.queryOperation = new QueryOperation<>(table);
    }

    public TableIterator<R, K> select(Column<R, K> column) {
        columns.add(column);
        return this;
    }

    @SafeVarargs
    public final TableIterator<R, K> select(Column<R, K>... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    public TableIterator<R, K> where(ExpressionRenderer<R, K> renderer) {
        queryOperation.where(renderer);
        return this;
    }

    public TableIterator<R, K> setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public TableIterator<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public TableIterator<R, K> index(Column<R, K> index) {
        this.queryOperation.onIndex(index);
        return this;
    }

    private void fetch() {

        if (finished) {
            this.result = Collections.emptyList();
            return;
        }

        try {
            ListResponse<R, K> response = queryOperation
                    .select(columns)
                    .exclusiveStartKey(lastKey)
                    .limit(chunkSize)
                    .execute(getClient());

            lastKey = response.getLastKey();

            this.result = response.items();

            if (result.size() < chunkSize) {
                finished = true;
            }

            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.result = Collections.emptyList();
    }

    @NotNull
    @Override
    public Iterator<R> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        if (result.isEmpty()) {
            fetch();

            if (result.isEmpty()) {
                finished = true;
            }
        }

        return !result.isEmpty();
    }

    @Override
    public R next() {
        if (result.isEmpty()) fetch();

        return result.remove(0);
    }
}
