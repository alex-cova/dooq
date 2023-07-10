package com.dooq.api;

import com.dooq.core.exception.DeserializationException;
import com.dooq.engine.ParserCompiler;
import com.dooq.util.ReflectionUtils;
import com.dooq.DynamoSL;
import com.dooq.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractRecord<T extends AbstractRecord<T>> {

    private transient Map<String, AttributeValue> representation;
    private transient Table<T, ?> table;

    private transient DynamoSL dsl;

    public void setDsl(DynamoSL dsl) {
        this.dsl = dsl;
    }

    public abstract Key getKey();

    @SuppressWarnings("unchecked")
    public final PutItemResponse store() {
        return dsl.store((T) this);
    }

    @SuppressWarnings("unchecked")
    public final PutItemResponse store(@NotNull DynamoSL dsl) {
        return dsl.store((T) this);
    }

    public final void update() {
        Objects.requireNonNull(dsl);
        dsl.update(this);
    }

    public Table<T, ?> getTable() {
        return table;
    }

    public AbstractRecord<T> setTable(Table<T, ?> table) {
        this.table = table;
        return this;
    }

    public void setRepresentation(Map<String, AttributeValue> representation) {
        this.representation = representation;
    }

    public final @Nullable Map<String, AttributeValue> getRepresentation() {
        return representation;
    }

    @SuppressWarnings("unchecked")
    public <R> R map(@NotNull Function<T, R> mapper) {
        return mapper.apply((T) this);
    }

    public final <K> @NotNull K into(Class<K> type) {

        var instance = ParserCompiler.getParser(type).newInstance();

        var fieldsMap = Arrays.stream(type.getDeclaredFields())
                .filter(a -> !Modifier.isTransient(a.getModifiers()) && !a.isAnnotationPresent(Transient.class))
                .collect(Collectors.toMap(Field::getName, c -> c));

        for (Field field : getClass().getDeclaredFields()) {

            Field target = fieldsMap.get(field.getName());

            if (target == null) {
                continue;
            }

            target.setAccessible(true);

            try {
                target.set(field.get(this), instance);
            } catch (IllegalAccessException e) {
                throw new DeserializationException(e);
            }
        }

        return instance;
    }
}
