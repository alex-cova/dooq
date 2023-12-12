package org.dooq.mapper;

import org.dooq.core.ItemParser;
import org.dooq.scheme.ProductRecord;
import org.dooq.scheme.Tables;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.math.BigDecimal;

public class ObjectMapperPerformance {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Fork(1)
    @Warmup(iterations = 1)
    @Benchmark
    public void name() {

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

        ItemParser.writeRecord(object);

    }
}
