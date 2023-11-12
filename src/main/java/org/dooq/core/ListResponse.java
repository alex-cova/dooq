package org.dooq.core;

import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.Key;
import org.dooq.api.Table;
import org.dooq.engine.ParserCompiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ListResponse<R extends AbstractRecord<R>, K extends Key> extends Response {

    Table<R, K> getTable();

    Map<String, AttributeValue> lastEvaluatedKey();

    List<Map<String, AttributeValue>> getItems();

    default List<R> items() {
        return into(getTable().getRecordType());
    }

    default R one() {
        return oneInto(getTable().getRecordType());
    }

    default <T> List<T> into(Class<T> type) {

        if (isEmpty()) return Collections.emptyList();

        var sameTable = false;

        if (AbstractRecord.class.isAssignableFrom(type)) {
            sameTable = getTable().getRecordType() == type;
        }

        var compiledParser = ParserCompiler.getParser(type);

        var items = getItems().stream()
                .map(compiledParser::parse)
                .collect(Collectors.toCollection(ArrayList::new));

        if (sameTable) {
            for (T item : items) {
                ((AbstractRecord<?>) item).$setTable(getTable());
            }
        }

        return items;
    }
    
    default @Nullable String getLastEvaluatedKey(Column<R, K> column) {
        return getLastKey().get(column);
    }

    @Contract(" -> new")
    default @NotNull LastEvaluatedKey getLastKey() {
        return new LastEvaluatedKey(lastEvaluatedKey());
    }

    default <T> @Nullable T oneInto(Class<T> type) {

        if (getItems().isEmpty()) {
            return null;
        }

        return ItemParser.readRecord(getItems().get(0), type);
    }
}
