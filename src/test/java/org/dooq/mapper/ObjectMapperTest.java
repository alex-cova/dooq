package org.dooq.mapper;

import org.dooq.core.ItemParser;
import org.dooq.scheme.ProductRecord;
import org.dooq.scheme.Tables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class ObjectMapperTest {

    @Test
    void testMapping() {

        var object = new ProductRecord()
                .setCompanyId(1L)
                .setUuid("uuid")
                .setDescription("Description")
                .setDetails("details")
                .setAutoWeigh(true)
                .setCategoryId("categoryId")
                .setDepartmentId("departmentId")
                .setBulk(true)
                .setCustomsName("customs")
                .setAvgPurchasePrice(new BigDecimal("12"));

        object.setTable(Tables.PRODUCT);

        var result = ItemParser.writeRecord(object);

        ProductRecord parsed = ItemParser.readRecord(result.map(), ProductRecord.class);

        Assertions.assertNotNull(parsed);
        Assertions.assertEquals(object.toString(), parsed.toString());

    }
}
