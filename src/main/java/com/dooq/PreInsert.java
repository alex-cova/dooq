package com.dooq;

import com.dooq.api.AbstractRecord;
import com.dooq.api.Table;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Objects;

public class PreInsert<R extends AbstractRecord<R>, K extends Key> {

    private final Table<R, K> table;
    private final DynamoDbClient dsl;

    public PreInsert(Table<R, K> table, DynamoDbClient dsl) {
        this.table = table;
        this.dsl = dsl;
    }

    public PutOperation<R, K> value(R value) {
        Objects.requireNonNull(value);

        return PutOperation.into(table)
                .value(value)
                .setClient(dsl);
    }

}
