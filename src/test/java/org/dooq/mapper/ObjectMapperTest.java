package org.dooq.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dooq.core.ItemParser;
import org.dooq.scheme.ProductRecord;
import org.dooq.scheme.Tables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class ObjectMapperTest {

    @Test
    void testMapping() throws JsonProcessingException {

        var mapper = new ObjectMapper();

        var record = new ProductRecord()
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

        record.setTable(Tables.PRODUCT);

        var result = ItemParser.writeRecord(record);

        ProductRecord parsed = ItemParser.readRecord(result.map(), ProductRecord.class);

        Assertions.assertNotNull(parsed);
        Assertions.assertEquals(mapper.writeValueAsString(record),
                mapper.writeValueAsString(parsed));

    }
}
