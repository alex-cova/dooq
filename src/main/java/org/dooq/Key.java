package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.api.Semantics;
import org.dooq.core.AttributeWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

public class Key extends HashMap<String, AttributeValue> {

    private String table;
    private String partitionKeyName;
    private String sortKeyName;

    public Key setTable(String table) {
        this.table = table;
        return this;
    }

    public String getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Key key = (Key) o;

        return key.getPartitionValue().equals(getPartitionValue()) && key.getSortValue().equals(getSortValue());
    }


    public static @NotNull Key of(Column<?, ?> partition, Object value, Column<?, ?> sort, Object value2) {
        var key = new Key();

        key.setPartitionKey(partition, value)
                .setSortingKey(sort, value2);

        return key;
    }

    public Key and(@NotNull Column<?, ?> column, Object value) {
        put(column.name(), AttributeWriter.parse(value));

        return this;
    }

    public Key setPartitionKey(@NotNull Column<?, ?> column, Object value) {
        setPartitionKey(column.name(), value);

        return this;
    }

    public Key setPartitionKey(String name, Object value) {
        Objects.requireNonNull(value, "Partition key '" + name + "' can't be null");

        if (size() > 0) {
            throw new IllegalStateException("Must specify the partition key first");
        }

        this.partitionKeyName = name;

        put(name, AttributeWriter.parse(value));

        return this;
    }

    public Key setSortingKey(@NotNull Column<?, ?> column, Object value) {
        return setSortingKey(column.name(), value);
    }

    public Key setSortingKey(@NotNull Column<?, ?> column, Object value, Object value2) {

        Objects.requireNonNull(value, "Value for sort key can't be null");
        Objects.requireNonNull(value, "Value2 for sort key can't be null");

        var key = value + Semantics.HASH + value2;

        setSortingKey(column.name(), key);

        return this;
    }

    public Key setPartitionKey(@NotNull Column<?, ?> column, Object value, Object value2) {
        Objects.requireNonNull(value, "Value for partition key can't be null");
        Objects.requireNonNull(value, "Value2 for partition key can't be null");

        var key = value + Semantics.HASH + value2;

        setPartitionKey(column.name(), key);

        return this;
    }

    public Key setSortingKey(String name, Object value) {

        Objects.requireNonNull(value, "Sort key '" + name + "' can't be null");

        if (size() == 0) {
            throw new IllegalStateException("Must specify the partition key first");
        }

        this.sortKeyName = name;

        put(name, AttributeWriter.parse(value));

        return this;
    }

    public <R extends AbstractRecord<R>, K extends Key> Key setSortingKey(@Nullable Map<Column<R, K>, Object> map) {

        if (map == null) return this;

        List<Entry<Column<R, K>, Object>> collect = new ArrayList<>(map.entrySet());
        Entry<Column<R, K>, Object> entry = collect.get(0);

        this.sortKeyName = entry.getKey().name();

        put(entry.getKey().name(), AttributeWriter.parse(entry.getValue()));

        return this;
    }

    public <R extends AbstractRecord<R>, K extends Key> Key setPartitionKeyName(@NotNull Map<Column<R, K>, Object> map) {
        List<Entry<Column<R, K>, Object>> collect = new ArrayList<>(map.entrySet());
        Entry<Column<R, K>, Object> entry = collect.get(0);

        this.partitionKeyName = entry.getKey().name();

        put(entry.getKey().name(), AttributeWriter.parse(entry.getValue()));

        return this;
    }

    public AttributeValue getPartitionValue() {
        return get(partitionKeyName);
    }

    public AttributeValue getSortValue() {
        return get(sortKeyName);
    }

    public String getPartitionKeyName() {
        return partitionKeyName;
    }

    public String getSortKeyName() {
        return sortKeyName;
    }

    @Override
    public String toString() {
        return "Key{" +
                "partitionKey='" + partitionKeyName + '\'' +
                ", sortKey='" + sortKeyName + '\'' +
                '}';
    }
}
