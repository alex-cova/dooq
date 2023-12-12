package org.dooq.core.response;

import org.dooq.api.DynamoRecord;
import org.dooq.api.Table;
import org.dooq.parser.ParserCompiler;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;

import java.util.Collections;
import java.util.List;

public record BatchGetMultiTableResponse(BatchGetItemResponse response) {

    public boolean isEmpty() {
        return !response.hasResponses();
    }

    public @NotNull <R extends DynamoRecord<R>> List<R> getItems(@NotNull Table<R, ?> table) {
        var items = response.responses()
                .get(table.getTableName());

        if (items == null) {
            return Collections.emptyList();
        }

        var converter = ParserCompiler.getConverter(table.getRecordType());

        return converter.readAll(items);
    }


}
