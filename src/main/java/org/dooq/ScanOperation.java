package org.dooq;

import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.core.AttributeWriter;
import org.dooq.core.DynamoOperation;
import org.dooq.core.ListResponse;
import org.dooq.core.exception.DynamoOperationException;
import org.dooq.core.response.BufferedScanResponse;
import org.dooq.engine.ExpressionCompiler;
import org.dooq.engine.ExpressionRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WARNING
 * <p>
 * In general, Scan operations are less efficient than other operations in DynamoDB. A Scan operation always scans the entire table or secondary index.
 * It then filters out values to provide the result you want, essentially adding the extra step of removing data from the result set.
 */
public class ScanOperation<R extends DynamoRecord<R>, K extends Key> extends DynamoOperation<R, K> {

    private ScanRequest.Builder builder;
    private Column<R, K> index;
    private final List<ExpressionRenderer<R, K>> expressionList;
    private boolean debug;

    public ScanOperation(@NotNull Table<R, K> table) {
        super(table);

        this.expressionList = new ArrayList<>();

        builder = ScanRequest.builder()
                .tableName(table.getTableName());


    }

    public ScanOperation<R, K> setClient(DynamoDbClient client) {
        this.client = client;
        return this;
    }

    public ScanOperation<R, K> limit(int limit) {

        builder = builder.limit(limit);

        return this;
    }

    public ScanOperation<R, K> exclusiveStartKey(K key) {
        builder = builder.exclusiveStartKey(key);

        return this;
    }

    public ScanOperation<R, K> exclusiveStartKey(Map<String, AttributeValue> key) {
        builder = builder.exclusiveStartKey(key);

        return this;
    }

    public ScanOperation<R, K> where(ExpressionRenderer<R, K> filterExpression) {
        expressionList.add(filterExpression);
        return this;
    }

    public <T> List<T> fetchInto(Class<T> type) {
        return execute(client)
                .into(type);
    }

    public ScanOperation<R, K> index(@NotNull Column<R, K> column) {
        this.index = column;
        return this;
    }

    public ScanOperation<R, K> debug() {
        this.debug = true;
        return this;
    }

    public ListResponse<R, K> execute() {
        return execute(client);
    }

    public ListResponse<R, K> execute(DynamoDbClient client) {

        if (index != null) {
            builder = builder.indexName(index.name());
        }

        if (expressionList.isEmpty()) return executeInternal(client);

        var compiled = ExpressionCompiler.compileForScan(getTable(), expressionList, index);

        builder = builder
                .filterExpression(compiled.expression())
                .expressionAttributeValues(AttributeWriter.parseMap(compiled.values()))
                .expressionAttributeNames(compiled.attributeNames());

        return executeInternal(client);
    }

    @Contract(" _ -> new")
    private @NotNull ListResponse<R, K> executeInternal(DynamoDbClient client) {
        var operation = builder.build();

        if (debug) {
            Logger.getLogger(ScanOperation.class.getName())
                    .log(Level.INFO, operation.toString());
        }

        try {
            return new BufferedScanResponse<>(client.scan(operation), getTable());
        } catch (Exception ex) {
            throw new DynamoOperationException(operation, ex);
        }
    }

}
