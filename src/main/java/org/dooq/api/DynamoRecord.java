package org.dooq.api;

import org.dooq.DynamoSL;
import org.dooq.Key;
import org.dooq.core.exception.DeserializationException;
import org.dooq.util.ReflectionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base class for all records.
 *
 * @author alex
 * @param <T>
 */
public abstract class DynamoRecord<T extends DynamoRecord<T>> {

    private transient Map<String, AttributeValue> representation;
    private transient Table<?, ?> table;
    private transient DynamoSL dsl;

    public void setDsl(DynamoSL dsl) {
        this.dsl = dsl;
    }

    @Transient
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
        Objects.requireNonNull(dsl, "unable to update record without dsl");
        dsl.update(this);
    }

    @SuppressWarnings("unchecked")
    @Transient
    public Table<T, ?> getTable() {
        return (Table<T, ?>) table;
    }

    public DynamoRecord<T> setTable(Table<T, ?> table) {
        this.table = table;
        return this;
    }

    @ApiStatus.Internal
    public void $setTable(Table<?, ?> table) {
        this.table = table;
    }

    public void setRepresentation(Map<String, AttributeValue> representation) {
        this.representation = representation;
    }

    @Transient
    public final @Nullable Map<String, AttributeValue> getRepresentation() {
        return representation;
    }

    @SuppressWarnings("unchecked")
    public <R> R map(@NotNull Function<T, R> mapper) {
        return mapper.apply((T) this);
    }

    public final <K> @NotNull K into(Class<K> type) {

        var instance = ReflectionUtils.newInstance(type);

        var targetMap = ReflectionUtils.mapFields(type);
        var sourceMap = ReflectionUtils.mapFields(getClass());

        for (Map.Entry<String, Field> targetEntry : targetMap.entrySet()) {

            Field source = targetEntry.getValue();

            Field target = sourceMap.get(source.getName());

            if (target == null) {
                continue;
            }

            target.setAccessible(true);

            try {
                target.set(source.get(this), instance);
            } catch (IllegalAccessException e) {
                throw new DeserializationException(e);
            }
        }

        return instance;
    }
}
