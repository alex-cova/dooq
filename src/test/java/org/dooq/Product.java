package org.dooq;

import org.dooq.api.*;
import org.dooq.core.schema.Index;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Product implements Table<ProductRecord, ProductKey> {

    public final StringField<String, ProductRecord, ProductKey> UUID = FieldBuilder.sort("uuid", String.class, this);
    public final Field<Long, ProductRecord, ProductKey> CONTENTID = FieldBuilder.partition("contentId", Long.class, this);
    public final Field<Long, ProductRecord, ProductKey> STOCK = FieldBuilder.partition("stock", Long.class, this);
    public final Field<String, ProductRecord, ProductKey> SKU = FieldBuilder.of("sku", String.class, this);
    public final Field<String, ProductRecord, ProductKey> DESCRIPTION = FieldBuilder.of("description", String.class, this);
    public final Field<String, ProductRecord, ProductKey> IMAGEURL = FieldBuilder.of("imageUrl", String.class, this);
    public final Field<String, ProductRecord, ProductKey> PURCHASEUNITID = FieldBuilder.of("purchaseUnitId", String.class, this);
    public final Field<String, ProductRecord, ProductKey> SALESUNITID = FieldBuilder.of("salesUnitId", String.class, this);
    public final Field<BigDecimal, ProductRecord, ProductKey> FACTOR = FieldBuilder.of("factor", BigDecimal.class, this);
    public final Field<Boolean, ProductRecord, ProductKey> BULK = FieldBuilder.of("bulk", Boolean.class, this);
    public final Field<Boolean, ProductRecord, ProductKey> AUTOWEIGH = FieldBuilder.of("autoWeigh", Boolean.class, this);
    public final Field<String, ProductRecord, ProductKey> DEPARTMENTID = FieldBuilder.of("departmentId", String.class, this);
    public final Field<String, ProductRecord, ProductKey> CATEGORYID = FieldBuilder.of("categoryId", String.class, this);

    public final Field<Boolean, ProductRecord, ProductKey> WASTE = FieldBuilder.of("waste", Boolean.class, this);

    private final List<Column<ProductRecord, ProductKey>> COLUMNS;
    private final Map<String, Index> INDICES;


    public Product() {
        COLUMNS = columns();

        INDICES = Index.builder(this)
                .localOnlyKeys(CATEGORYID)
                .localOnlyKeys(DEPARTMENTID)
                .localOnlyKeys(PURCHASEUNITID)
                .localOnlyKeys(SALESUNITID)
                .globalInclude("category", CONTENTID, CATEGORYID, Set.of(IMAGEURL, SALESUNITID, AUTOWEIGH, DESCRIPTION, SKU, BULK))
                .globalInclude("type", CONTENTID, CATEGORYID, Set.of(IMAGEURL, SALESUNITID, AUTOWEIGH, DESCRIPTION, SKU, BULK, CATEGORYID))
                .build();
    }

    @Override
    public Map<String, Index> getIndices() {
        return INDICES;
    }


    @Override
    public List<Column<ProductRecord, ProductKey>> getColumns() {
        return COLUMNS;
    }

    @Override
    public String getTableName() {
        return "product";
    }

    @Override
    public Class<ProductRecord> getRecordType() {
        return ProductRecord.class;
    }

    @Override
    public Column<ProductRecord, ProductKey> getPartitionColumn() {
        return CONTENTID;
    }

    @Override
    public Column<ProductRecord, ProductKey> getSortColumn() {
        return UUID;
    }

}
