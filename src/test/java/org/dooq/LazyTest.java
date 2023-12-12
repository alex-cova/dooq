package org.dooq;

import org.dooq.api.AbstractKey;
import org.dooq.api.AbstractRecord;
import org.dooq.api.AbstractTable;
import org.junit.jupiter.api.Test;

public class LazyTest {

    final FakeDynamoDBClient client = new FakeDynamoDBClient();
    final DynamoSL dsl = new DynamoSL(client);

    @Test
    void testLazyList() {
        AbstractRecord record = dsl.selectFrom(new AbstractTable("join"))
                .withKey(new AbstractKey("partition", "value"))
                .fetch();

    }
}
