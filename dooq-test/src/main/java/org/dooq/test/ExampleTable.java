package org.dooq.test;

import org.dooq.api.*;

import java.math.BigDecimal;

@LocalIndex(name = "category-index", sortKey = "categoryId", projectionMode = ProjectionMode.ALL)
@GlobalIndex(name = "sku-index", partitionKey = "sku", projectionMode = ProjectionMode.ALL)
@DynamoDBTable("Products")
public class ExampleTable {
    @PartitionKey(alias = "pk")
    private String partition;
    @SortKey(alias = "sk")
    private String sort;
    private String description;
    private BigDecimal price;
    private String sku;
    @ColumnAlias("categoryId")
    private String categoryUuid;

}
