package org.dooq;

import org.dooq.core.ItemParser;
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
                .setContentId(1L)
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

    }
}
