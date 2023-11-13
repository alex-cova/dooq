package org.dooq.test;

import org.dooq.DynamoSL;

import static org.dooq.test.Tables.PRODUCTS;

public class ClientCodeExample {

    public static void main(String[] args) {

        var dsl = new DynamoSL(null);

        var products = dsl.selectFrom(PRODUCTS)
                .where(PRODUCTS.SORT.startsWith("product#"))
                .fetchInto(ProductsRecord.class);

    }
}
