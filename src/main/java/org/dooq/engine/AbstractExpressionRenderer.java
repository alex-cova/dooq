package org.dooq.engine;

import org.dooq.Key;
import org.dooq.api.Column;
import org.dooq.api.DynamoRecord;
import org.dooq.core.AttributeWriter;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractExpressionRenderer<R extends DynamoRecord<R>, K extends Key> implements ExpressionRenderer<R, K> {

    @Override
    public Map<String, String> getAttributeNames() {
        return columns()
                .stream()
                .collect(Collectors.toMap(Column::escapedName, Column::name));
    }

    @Override
    public Map<String, AttributeValue> getAttributeValues() {

        var map = new HashMap<String, AttributeValue>();

        for (Map.Entry<Column<R, K>, Object> entry : getValue().entrySet()) {
            map.put(entry.getKey().param(), AttributeWriter.parse(entry.getValue()));
        }

        return map;
    }

    @Override
    public boolean containsIndex(@Nullable Column<R, K> index) {

        if (index == null) return false;

        return columns().stream()
                .anyMatch(a -> a.name().equals(index.name()));
    }

}
