package org.dooq.test;

import org.dooq.api.DynamoDBTable;
import org.dooq.api.PartitionKey;

import java.util.Set;

@DynamoDBTable("ProductCatalog")
public class CatalogItem {
    @PartitionKey
    private Integer id;
    private String title;
    private String ISBN;
    private Set<String> bookAuthors;

}