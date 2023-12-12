package org.dooq;

import org.junit.jupiter.api.Test;

import static org.dooq.scheme.Tables.MIXER;
import static org.dooq.scheme.Tables.PRODUCT;

public class JoinTest {

    final FakeDynamoDBClient client = new FakeDynamoDBClient();
    final DynamoSL dsl = new DynamoSL(client);

    @Test
    void testLateJoin() {

        dsl.selectFrom(PRODUCT)
                .lateJoin(MIXER.on(MIXER.UUID.startsWith(PRODUCT.UUID))
                        .and(MIXER.COMPANYID.eq(PRODUCT.DEPARTMENTID)))
                .where(PRODUCT.COMPANYID.eq(123L))
                .limit(20)
                .fetch();
    }

    @Test
    void testSimpleEagerJoin() {
        dsl.selectFrom(PRODUCT)
                .join(MIXER)
                .where(PRODUCT.COMPANYID.eq(123L))
                .limit(20)
                .fetch();
    }

    @Test
    void testComplexEagerJoin() {
        dsl.selectFrom(PRODUCT)
                .join(MIXER.on(MIXER.COMPANYID.eq(PRODUCT.DEPARTMENTID)))
                .where(PRODUCT.COMPANYID.eq(123L))
                .limit(20)
                .fetch();
    }
}
