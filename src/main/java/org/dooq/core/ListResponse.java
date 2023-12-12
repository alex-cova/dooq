package org.dooq.core;

import org.dooq.Key;
import org.dooq.api.Column;
import org.dooq.api.DynamoConverter;
import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.parser.ObjectParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ListResponse<R extends DynamoRecord<R>, K extends Key> extends Response {

    Table<R, K> getTable();

    Map<String, AttributeValue> lastEvaluatedKey();

    List<Map<String, AttributeValue>> getItems();

    default @NotNull List<R> items() {

        if (isEmpty()) return Collections.emptyList();

        var parser = getTable().getRecordParser();

        return getItems().stream()
                .map(parser::read)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    default @Nullable R one() {

        if (isEmpty()) return null;

        return getTable().getRecordParser()
                .read(getItems().get(0));

    }

    default <T> @Nullable T one(@NotNull ObjectParser<T> parse) {
        if (isEmpty()) return null;

        return parse.read(getItems().get(0));
    }

    default <T> @NotNull List<T> into(Class<T> type) {

        if (isEmpty()) return Collections.emptyList();

        var sameTable = false;

        if (DynamoRecord.class.isAssignableFrom(type)) {
            sameTable = getTable().getRecordType() == type;
        }

        var compiledParser = DynamoConverter.getConverter(type);

        var items = getItems().stream()
                .map(compiledParser::read)
                .collect(Collectors.toCollection(ArrayList::new));

        if (sameTable) {
            for (T item : items) {
                ((DynamoRecord<?>) item).$setTable(getTable());
            }
        }

        return items;
    }

    default <T> @NotNull List<T> into(ObjectParser<T> parser) {

        if (isEmpty()) return Collections.emptyList();

        return getItems().stream()
                .map(parser::read)
                .collect(Collectors.toCollection(ArrayList::new));
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
