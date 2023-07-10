package com.dooq;

import com.dooq.engine.ExpressionRenderer;
import org.junit.jupiter.api.Test;

public class JoinTest {

    final FakeDynamoDBClient client = new FakeDynamoDBClient();
    final DynamoSL dsl = new DynamoSL(client);

    @Test
    void testLateJoin() {

        dsl.selectFrom(Tables.PRODUCT)
                .lateJoin(Tables.MIXER.on(Tables.MIXER.UUID.startsWith(Tables.PRODUCT.UUID))
                        .samePartition())
                .where(Tables.PRODUCT.CONTENTID.eq(123))
                .limit(20)
                .fetch();
    }

    @Test
    void testSimpleEagerJoin() {
        dsl.selectFrom(Tables.PRODUCT)
                .join(Tables.MIXER)
                .where(Tables.PRODUCT.CONTENTID.eq(123))
                .limit(20)
                .fetch();
    }

    @Test
    void testComplexEagerJoin() {
        dsl.selectFrom(Tables.PRODUCT)
                .join(dsl.selectFrom(Tables.MIXER)
                        .where(Tables.MIXER.CONTENTID.eq(Tables.PRODUCT.CONTENTID)))
                .where(Tables.PRODUCT.CONTENTID.eq(123))
                .limit(20)
                .fetch();
    }
}
