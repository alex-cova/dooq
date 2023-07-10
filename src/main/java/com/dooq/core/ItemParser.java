package com.dooq.core;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import com.dooq.engine.ParserCompiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemParser {

    @Contract("_ -> new")
    public static <T extends AbstractRecord<T>> @NotNull ParseResult writeRecord(@NotNull T object) {
        return write(object);
    }

    @Contract("_ -> new")
    public static @NotNull ParseResult write(@NotNull AbstractRecord<?> object) {
        var fields = object.getClass().getDeclaredFields();

        if (fields.length == 0) {
            throw new IllegalStateException("No fields for type: " + object.getClass());
        }

        var table = object.getTable();

        var columnMap = table.getColumns()
                .stream()
                .collect(Collectors.toMap(Column::name, v -> v));

        var map = new HashMap<String, AttributeValue>();

        for (Field field : fields) {
            try {

                if (Modifier.isTransient(field.getModifiers()) || field.isAnnotationPresent(Transient.class)) {
                    continue;
                }

                if (!columnMap.containsKey(field.getName())) {
                    System.err.println("WARNING field: " + field.getName() + " is not present in table, columns: " + columnMap.size());
                }

                field.setAccessible(true);

                var value = field.get(object);

                map.put(field.getName(), AttributeWriter.parse(value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return new ParseResult(map, object.getKey());
    }

    public static <T> @Nullable T readRecord(@NotNull Map<String, AttributeValue> map, Class<T> type) {

        if (map.isEmpty()) {
            return null;
        }

        return ParserCompiler.getParser(type)
                .parse(map);
    }
}
