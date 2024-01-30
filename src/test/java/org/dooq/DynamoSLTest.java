package org.dooq;

import org.dooq.parser.ParserCompiler;
import org.dooq.scheme.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.List;
import java.util.Map;

import static org.dooq.scheme.Tables.MIXER;
import static org.dooq.scheme.Tables.PRODUCT;

public class DynamoSLTest {

    final FakeDynamoDBClient client = new FakeDynamoDBClient();
    final DynamoSL dsl = new DynamoSL(client);

    @Test
    void batchStore() {

        var record1 = dsl.newRecord(PRODUCT).setCompanyId(1L).setUuid("1234");
        var record2 = dsl.newRecord(PRODUCT).setCompanyId(1L).setUuid("1234");
        var record3 = dsl.newRecord(PRODUCT).setCompanyId(1L).setUuid("1234");
        var record4 = dsl.newRecord(PRODUCT).setCompanyId(1L).setUuid("1234");
        var record5 = dsl.newRecord(PRODUCT).setCompanyId(1L).setUuid("1234");

        dsl.store(List.of(record1, record2, record3, record4, record5));
    }

    @Test
    void checkQueryGetItemConvert() {

        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("abcdef")))
                .fetchOne();

        client.assertLastGetItemRequest();
    }

    @Test
    void batchUpdate() {

        dsl.delete(ProductKey.of(1, "123"));
        dsl.delete(ProductKey.of(1, "123"), ProductKey.of(1, "124"));
        dsl.delete(ProductKey.of(1, "123"), ProductKey.of(1, "124"), ProductKey.of(1, "125"));
        dsl.delete(ProductKey.of(1, "123"), ProductKey.of(1, "124"), MixerKey.of("1", "125"));

        dsl.delete(dsl.deleteFrom(PRODUCT)
                        .where(PRODUCT.COMPANYID.eq(1L)),
                dsl.deleteFrom(MIXER)
                        .where(MIXER.COMPANYID.eq("1")));

        dsl.deleteFrom(PRODUCT)
                .key(key -> key.partition(1).sort("123"));

    }

    @Test
    void checkGetItem() {
        dsl.selectFrom(PRODUCT)
                .withKey(ProductKey.of(1, "abcfdef"))
                .execute();

        client.assertLastGetItemRequest();
    }

    @Test
    void incrementColumn() {

        dsl.update(PRODUCT)
                .increment(PRODUCT.STOCK)
                .where(PRODUCT.UUID.eq("1234"));

    }

    @Test
    void decrementColumn() {

        dsl.update(PRODUCT)
                .decrement(PRODUCT.STOCK)
                .where(PRODUCT.UUID.eq("1234"));
    }

    @Test
    void appendToList() {

        dsl.update(PRODUCT)
                .append(PRODUCT.STOCK, "value")
                .where(PRODUCT.UUID.eq("1234")
                        .and(PRODUCT.COMPANYID.eq(1L)));

        dsl.update(PRODUCT)
                .append(PRODUCT.STOCK, "value")
                .key(key -> key.partition(1)
                        .sort("1234"));
    }

    @Test
    void queryWithFilters() {
        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("abcdef"))
                        .and(PRODUCT.CATEGORYID.eq("category")))
                .fetchOne();

        client.assertLastQueryRequest();

        final QueryRequest request = client.getLastRequest();

        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("abcdef")
                                .and(PRODUCT.CATEGORYID.eq("category"))))
                .fetchOne();

        final QueryRequest another = client.getLastRequest();

        Assertions.assertEquals(request.toString(), another.toString());

    }

    @Test
    void testComplexQuery() {
        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("uuid"))
                        .and(PRODUCT.PURCHASEUNITID.in(List.of("salesUnit")))
                        .and(PRODUCT.CATEGORYID.eq("category")
                                .or(PRODUCT.DEPARTMENTID.eq("department"))))
                .execute();

        Assertions.assertTrue(client.getQueryRequest().filterExpression().contains("("));
        Assertions.assertTrue(client.getQueryRequest().filterExpression().contains(")"));
        Assertions.assertFalse(client.getQueryRequest().filterExpression().contains("IN ("));

        System.out.println("---");

        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("uuid"))
                        .and(PRODUCT.PURCHASEUNITID.in(List.of("salesUnit")))
                        .and(PRODUCT.CATEGORYID.eq("category"))
                        .and(PRODUCT.DEPARTMENTID.eq("department")))
                .execute();

        Assertions.assertFalse(client.getQueryRequest().filterExpression().contains("("));
        Assertions.assertFalse(client.getQueryRequest().filterExpression().contains(")"));
    }

    @Test
    void testComplex2() {
        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("uuid"))
                        .and(PRODUCT.PURCHASEUNITID.in(List.of("salesUnit", "purchaseUnit")))
                        .and(PRODUCT.CATEGORYID.eq("category")
                                .or(PRODUCT.DEPARTMENTID.eq("department"))))
                .execute();

        Assertions.assertTrue(client.getQueryRequest().filterExpression().contains("("));
        Assertions.assertTrue(client.getQueryRequest().filterExpression().contains(")"));
        Assertions.assertTrue(client.getQueryRequest().filterExpression().contains("IN ("));
    }

    @Test
    void checkDelete() {
        dsl.deleteFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("uuid")))
                .execute();
    }

    @Test
    void checkFetchExists() {
        dsl.fetchExists(dsl.selectFrom(PRODUCT)
                .withKey(ProductKey.of(1, "1")));
    }

    @Test
    void expressionAttributeValues() {
        dsl.select(PRODUCT.DESCRIPTION)
                .from(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L))
                .fetchInto(String.class);

        Map<String, AttributeValue> map = client.getQueryRequest()
                .expressionAttributeValues();

        Assertions.assertFalse(map.isEmpty());
        Assertions.assertNull(client.getQueryRequest().filterExpression());
    }

    @Test
    void getByKey() {
        dsl.selectFrom(PRODUCT)
                .withKey(ProductKey.of(1, "1"))
                .execute();
    }

    @Test
    void missingExpressionName() {
        dsl.fetchExists(dsl.selectFrom(PRODUCT)
                .onIndex(PRODUCT.DEPARTMENTID)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.DEPARTMENTID.eq("uuid"))));

        Assertions.assertFalse(client.getQueryRequest().expressionAttributeNames().isEmpty());
    }

    @Test
    void missingKeyCondition() {
        dsl.selectFrom(PRODUCT)
                .onIndex(PRODUCT.CATEGORYID)
                .where(PRODUCT.CATEGORYID.eq("uuid")
                        .and(PRODUCT.COMPANYID.eq(1L)))
                .fetchInto(ProductRecord.class);
    }

    @Test
    void getFromTransform() {
        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.UUID.eq("uuid").and(PRODUCT.COMPANYID.eq(1L)))
                .execute();

        client.assertLastGetItemRequest();
    }

    void testQueryFrom() {
        dsl.selectFrom(PRODUCT)
                .onLocalIndex(PRODUCT.COMPANYID)
                .where(PRODUCT.COMPANYID.eq(1L))
                .startingFrom(ProductKey.of(1, "123"))
                .startingFrom(key -> key.partition(1).sort("absc"))
                .execute();
    }

    @Test
    void testBeginsKey() {

        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.startsWith("SEL")))
                .execute();

    }

    @Test
    void fetchExists() {

        dsl.fetchExists(dsl.selectFrom(PRODUCT)
                .where(Tables.PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.SKU.eq("sku"))));

    }

    @Test
    void testWhile() {

        List<ProductRecord> productRecords = dsl.selectFrom(PRODUCT)
                .until(PRODUCT.COMPANYID.eq(1L))
                .startingFrom(key -> key.partition("1")
                        .sort(1234))
                .limit(100)
                .fetchInto(ProductRecord.class);

    }

    @Test
    void fetch2Attributes() {
        dsl.select(PRODUCT.CATEGORYID, PRODUCT.BULK)
                .from(PRODUCT)
                .withKey(ProductKey.of(1, "1"))
                .fetch();

        dsl.select(PRODUCT.CATEGORYID, PRODUCT.BULK)
                .from(PRODUCT)
                .withKey(key -> key.partition(1).sort("1"))
                .fetch();
    }

    @Test
    void indexTest() {
        dsl.fetchExists(dsl.selectFrom(MIXER)
                .onIndex(MIXER.TAGNAME)
                .where(MIXER.PARENTUUID.eq("parentId")
                        .and(MIXER.TAGNAME.eq("tagName"))));

        Assertions.assertTrue(client.getQueryRequest().keyConditionExpression().contains("AND"));
    }

    @Test
    void convertUsingSpecifiedConverter() {

        var parser = ParserCompiler.getConverter(MixerRecord.class);

        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("uuid")))
                .execute();

        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("uuid")))
                .fetch(parser);

        dsl.selectFrom(PRODUCT)
                .where(PRODUCT.COMPANYID.eq(1L)
                        .and(PRODUCT.UUID.eq("uuid")))
                .fetchOne(parser);

        dsl.selectFrom(PRODUCT)
                .withKey(ProductKey.of(1, "1"))
                .fetch(parser);

    }
}
