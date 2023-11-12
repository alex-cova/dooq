package org.dooq.core;

import org.dooq.api.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public record LastEvaluatedKey(Map<String, AttributeValue> key) {

    public @Nullable String get(@NotNull Column<?, ?> column) {

        if (key == null) return null;

        if (key.isEmpty()) return null;

        return AttributeWriter.unwrap(key.get(column.name()))
                .toString();
    }

}
