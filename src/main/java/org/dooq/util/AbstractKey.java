package org.dooq.util;

import org.dooq.api.Column;
import org.dooq.Key;
import org.dooq.api.Table;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public final class AbstractKey extends Key {

    public AbstractKey() {
    }

    public AbstractKey(@NotNull Column<?, ?> partitionName, @NotNull Object partition,
                       @NotNull Column<?, ?> sortColumn, @NotNull Object sortKey) {

        this(partitionName.name(), partition, sortColumn.name(), sortKey);
    }

    public AbstractKey(@NotNull String partitionName, @NotNull Object partition,
                       @NotNull String sortName, @NotNull Object sortKey) {

        setPartitionKey(partitionName, partition);
        setSortingKey(sortName, sortKey);
    }

    public AbstractKey(@NotNull Table<?, ?> table, @NotNull Map<String, AttributeValue> map) {

        setPartitionKey(table.getPartitionColumn().name(), map.get(table.getPartitionColumn().name()));

        if (map.keySet().size() > 1) {
            setSortingKey(table.getSortColumn().name(), map.get(table.getSortColumn().name()));
        }

    }
}
