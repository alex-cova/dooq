package org.dooq.test;

import org.dooq.DynamoSL;

import java.math.BigDecimal;

import static org.dooq.test.Tables.PRODUCTS;

public class ClientCodeExample {

    public static void main(String[] args) {

        var dsl = new DynamoSL(null);

        var products = dsl.selectFrom(PRODUCTS)
                .where(PRODUCTS.SORT.startsWith("product#")
                        .and(PRODUCTS.SALEABLE.isTrue())
                        .and(PRODUCTS.SORT.isNull()))
                .fetchInto(ProductsRecord.class);

        var records = dsl.selectFrom(PRODUCTS)
                .onIndex("partition-sort-index")
                .where(PRODUCTS.CATEGORYUUID.eq("Book")
                        .and(PRODUCTS.PRICE.lessOrEqual(BigDecimal.TEN)))
                .limit(10)
                .fetch();


    }
}
