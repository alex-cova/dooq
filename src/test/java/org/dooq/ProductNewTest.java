package org.dooq;

import org.dooq.api.Column;
import org.dooq.api.Field;
import org.dooq.api.FieldBuilder;
import org.dooq.api.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductNewTest implements Table<ProductRecord, ProductKey> {

    public final Field<String, ProductRecord, ProductKey> UUID = FieldBuilder.partitionKey("uuid", String.class, this);
    public final Field<Long, ProductRecord, ProductKey> CONTENTID = FieldBuilder.sortKey("contentId", Long.class, this);
    public final Field<String, ProductRecord, ProductKey> SKU = FieldBuilder.of("sku", String.class, this);
    public final Field<Set<String>, ProductRecord, ProductKey> SKUS = FieldBuilder.ofSet("skus", String.class, this);
    public final Field<String, ProductRecord, ProductKey> DESCRIPTION = FieldBuilder.of("description", String.class, this);
    public final Field<String, ProductRecord, ProductKey> IMAGEURL = FieldBuilder.of("imageUrl", String.class, this);
    public final Field<String, ProductRecord, ProductKey> DETAILS = FieldBuilder.of("details", String.class, this);
    public final Field<String, ProductRecord, ProductKey> PURCHASEUNITID = FieldBuilder.of("purchaseUnitId", String.class, this);
    public final Field<String, ProductRecord, ProductKey> SALESUNITID = FieldBuilder.of("salesUnitId", String.class, this);
    public final Field<BigDecimal, ProductRecord, ProductKey> FACTOR = FieldBuilder.of("factor", BigDecimal.class, this);
    public final Field<Boolean, ProductRecord, ProductKey> BULK = FieldBuilder.of("bulk", Boolean.class, this);
    public final Field<Boolean, ProductRecord, ProductKey> AUTOWEIGH = FieldBuilder.of("autoWeigh", Boolean.class, this);
    public final Field<Boolean, ProductRecord, ProductKey> FAVORITE = FieldBuilder.of("favorite", Boolean.class, this);
    public final Field<String, ProductRecord, ProductKey> CLAVESAT = FieldBuilder.of("claveSat", String.class, this);
    public final Field<String, ProductRecord, ProductKey> DEPARTMENTID = FieldBuilder.of("departmentId", String.class, this);
    public final Field<String, ProductRecord, ProductKey> CATEGORYID = FieldBuilder.of("categoryId", String.class, this);
    public final Field<BigDecimal, ProductRecord, ProductKey> WASTE = FieldBuilder.of("waste", BigDecimal.class, this);
    public final Field<Integer, ProductRecord, ProductKey> TYPE = FieldBuilder.of("type", Integer.class, this);
    public final Field<BigDecimal, ProductRecord, ProductKey> WEIGHT = FieldBuilder.of("weight", BigDecimal.class, this);
    public final Field<Boolean, ProductRecord, ProductKey> SALEABLE = FieldBuilder.of("saleable", Boolean.class, this);
    public final Field<String, ProductRecord, ProductKey> CUSTOMSNAME = FieldBuilder.of("customsName", String.class, this);
    public final Field<LocalDateTime, ProductRecord, ProductKey> CUSTOMSDOCDATE = FieldBuilder.of("customsDocDate", LocalDateTime.class, this);
    public final Field<String, ProductRecord, ProductKey> CUSTOMSDOCNUMBER = FieldBuilder.of("customsDocNumber", String.class, this);
    public final Field<String, ProductRecord, ProductKey> PROPERTYACCOUNT = FieldBuilder.of("propertyAccount", String.class, this);
    public final Field<BigDecimal, ProductRecord, ProductKey> MINPROFITPERCENTAGE = FieldBuilder.of("minProfitPercentage", BigDecimal.class, this);
    public final Field<BigDecimal, ProductRecord, ProductKey> AVGPURCHASEPRICE = FieldBuilder.of("avgPurchasePrice", BigDecimal.class, this);
    public final Field<LocalDateTime, ProductRecord, ProductKey> CREATED = FieldBuilder.of("created", LocalDateTime.class, this);
    public final Field<String, ProductRecord, ProductKey> USERCREATORID = FieldBuilder.of("userCreatorId", String.class, this);
    public final Field<LocalDateTime, ProductRecord, ProductKey> MODIFIED = FieldBuilder.of("modified", LocalDateTime.class, this);
    public final Field<Map<String, BigDecimal>, ProductRecord, ProductKey> PRICES = FieldBuilder.ofMap("prices", String.class, BigDecimal.class, this);
    public final Field<Integer, ProductRecord, ProductKey> SUBSCRIPTIONUNIT = FieldBuilder.of("subscriptionUnit", Integer.class, this);
    public final Field<Integer, ProductRecord, ProductKey> SUBSCRIPTIONAMOUNT = FieldBuilder.of("subscriptionAmount", Integer.class, this);
    public final Field<String, ProductRecord, ProductKey> USERMODIFIERID = FieldBuilder.of("userModifierId", String.class, this);

    @Override
    public List<Column<ProductRecord, ProductKey>> getColumns() {
        return null;
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
