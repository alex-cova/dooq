package com.dooq.core.schema;

import com.dooq.Key;
import com.dooq.api.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record Index(String name, IndexType type, String partitionKey, String sortKey, IndexMode mode,
                    @Nullable Set<String> projections) {

    public boolean isKey(@NotNull Column<?, ?> column) {
        return isKey(column.name());
    }

    public boolean isKey(String name) {
        return partitionKey.equals(name) || sortKey.equals(name);
    }

    @Contract("_ -> new")
    public static <R extends AbstractRecord<R>, K extends Key> @NotNull Builder<R, K> builder(Table<R, K> table) {
        return new Builder<>(table);
    }

    public static class Builder<R extends AbstractRecord<R>, K extends Key> {

        private final Map<String, Index> map = new HashMap<>();
        private final Table<R, K> table;

        public Builder(Table<R, K> table) {
            this.table = table;
        }

        public Builder<R, K> globalAll(@NotNull Column<R, K> name, @NotNull Column<R, K> partition, @NotNull Column<R, K> sort) {
            return globalAll(name.name(), partition, sort);
        }

        public Builder<R, K> globalAll(String name, @NotNull Column<R, K> partition, @NotNull Column<R, K> sort) {
            var index = new Index(name, IndexType.GLOBAL, partition.name(),
                    sort.name(), IndexMode.ALL, Collections.emptySet());

            map.put(name, index);

            return this;
        }

        public Builder<R, K> globalOnlyKeys(@NotNull Column<R, K> name, @NotNull Column<R, K> partition, @NotNull Column<R, K> sort) {
            return globalOnlyKeys(name.name(), partition, sort);
        }

        public Builder<R, K> globalOnlyKeys(String name, @NotNull Column<R, K> partition, @NotNull Column<R, K> sort) {
            var index = new Index(name, IndexType.GLOBAL, partition.name(),
                    sort.name(), IndexMode.ONLY_KEYS, Collections.emptySet());

            map.put(name, index);

            return this;
        }

        public Builder<R, K> globalInclude(@NotNull Column<R, K> name, @NotNull Column<R, K> partition, @NotNull Column<R, K> sort, @NotNull Set<Column<R, K>> projections) {
            return globalInclude(name.name(), partition, sort, projections);
        }

        public Builder<R, K> globalInclude(String name, @NotNull Column<R, K> partition, @NotNull Column<R, K> sort, @NotNull Set<Column<R, K>> projections) {
            var index = new Index(name, IndexType.GLOBAL, partition.name(),
                    sort.name(), IndexMode.INCLUDE, projections.stream()
                    .map(Column::name)
                    .collect(Collectors.toSet()));

            map.put(name, index);

            return this;
        }

        public Builder<R, K> localAll(@NotNull Column<R, K> sort) {
            var index = new Index(sort.name(), IndexType.LOCAL, table.getPartitionColumn().name(),
                    sort.name(), IndexMode.ALL, Collections.emptySet());

            map.put(sort.name(), index);

            return this;
        }

        public Builder<R, K> localOnlyKeys(@NotNull Column<R, K> sort) {
            var index = new Index(sort.name(), IndexType.LOCAL, table.getPartitionColumn().name(),
                    sort.name(), IndexMode.ONLY_KEYS, Collections.emptySet());

            map.put(sort.name(), index);

            return this;
        }

        public Builder<R, K> localInclude(@NotNull Column<R, K> sort, @NotNull Set<Column<R, K>> projections) {
            var index = new Index(sort.name(), IndexType.LOCAL, table.getPartitionColumn().name(),
                    sort.name(), IndexMode.INCLUDE, projections.stream()
                    .map(Column::name)
                    .collect(Collectors.toSet()));

            map.put(sort.name(), index);

            return this;
        }

        public Map<String, Index> build() {

            var localIndices = map.values()
                    .stream()
                    .filter(a -> a.type == IndexType.LOCAL)
                    .count();

            var globalIndices = map.values()
                    .stream()
                    .filter(a -> a.type == IndexType.GLOBAL)
                    .count();

            if (localIndices > 5) {
                throw new IllegalStateException("Only 5 local indices are allowed");
            }

            if (globalIndices > 20) {
                throw new IllegalStateException("Only 20 global indices are allowed");
            }

            return Collections.unmodifiableMap(map);
        }

    }
}
