# DOOQ (Preview)

DynamoDB Object-Oriented Query

**Features:**

- Optimized read operations
- Limitations-aware queries
- Intelligent query builder
- Declarative syntax
- Zero-cost item to class parsing
- Automatic Schema generation

```
implementation("org.dooq:dooq:1.0.0-SNAPSHOT")
annotationProcessor("org.dooq:dooq-processor:1.0.0-SNAPSHOT")
```

# DynamoDSL

A domain specific language for DynamoDB, it uses a table specification which is auto-generated
by the annotation processor and is used to made read optimizations and validations
at runtime, however it can be used without the schema definition.

Inspired on JOOQ, running on top of
the [DynamoDB low level API](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Programming.LowLevelAPI.html)

# The mapper

Generates converter classes (Java 17) for POJOs/Recods at runtime, using ASM.

## Features

* Fast ⚡️
* Easy to use
* No reflection used at conversion time
* Little memory footprint
* Easy to add additional converters
* Support for java records

## Benchmark

```
Benchmark                          Mode  Cnt     Score   Error  Units
ConverterBenchmark.readBenchmark   avgt    5   635.997 ± 1.419  ns/op
ConverterBenchmark.writeBenchmark  avgt    5  1071.437 ± 4.250  ns/op
```

### Requirements

* Target class must have a default constructor
* Target class must have getters and setters for all fields to parse

if you want to omit some fields, you can use `@DynamoIgnore` annotation or `transient` keyword on field,
**this doesn't apply to records.**

# Key Differences

Based on: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.CRUDExample1.html

**AWS:**

```java

@DynamoDBTable(tableName = "ProductCatalog")
public class CatalogItem {
    private Integer id;
    private String title;
    private String ISBN;
    private Set<String> bookAuthors;

    // Partition key
    @DynamoDBHashKey(attributeName = "Id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "Title")
    public String getTitle() {
        return title;
    }
}
```

**DynamoDSL**

```java

@DynamoDBTable("ProductCatalog")
public class CatalogItem {
    @PartitionKey
    private Integer id;
    private String title;
    private String ISBN;
    private Set<String> bookAuthors;

}
```

> The table specification is used to check syntax at compilation time, uniformity at testing time, and optimizations at
> runtime.

**IMPORTANT NOTE:** Specifying a table structure doesn't mean that we must use one structure for one table, we can also
create another table specification reusing the table name, at the end what defines the structure is what We call, the
item or the **record** class.

## Store

**DynamoDB-Mapper**

```java
        CatalogItem item=new CatalogItem();
        item.setId(601);
        item.setTitle("Book 601");
        item.setISBN("611-1111111111");
        item.setBookAuthors(Set.of("Author1","Author2"));

// Save the item (book).
        DynamoDBMapper mapper=new DynamoDBMapper(client);
        mapper.save(item);

```

> Pretty easy isn't?

**DynamoDSL**

```java

dsl.newRecord(Tables.PRODUCTCATALOG).setId(601);
        .setTitle("Book 601");
        .setISBN("611-1111111111");
        .setBookAuthors(Set.of("Author1","Author2"));
        .store();
```

or

```java
dsl.insertInto(CATALOGINTEM)
        .value(somePojo)
        .key(SomeKey)
        .execute();
```

## Retrieve

**AWS**

```java
CatalogItem itemRetrieved=mapper.load(CatalogItem.class,601);
```

**DynamoDSL**

```java
CatalogItem itemRetrieved=dsl.selectFrom(CATALOGITEM)
        .withKey(CatalogItemKey.of(601))
        .fetch();
```

or

```java
CatalogItem itemRetrieved=dsl.selectFrom(CATALOGITEM)
        .where(CATALOGITEM.ID.eq(601))
        .fetchOne();
```

> The parameter of method withKey specs CatalogItemKey type only, and fetch only returns the table record type

and maybe I only want the title...

```java
String title=dsl.select(CatalogItem.TITLE)
        .from(CATALOGITEM)
        .withKey(CatalogItemKey.of(601))
        .fetch();
```

> This operation automatically defines a projection expression to only retrieve the title attribute

```java
CatalogItem itemRetrieved=dsl.selectFrom(CATALOGITEM)
        .where(CatalogItem.ID.eq(601))
        .fetchOne();
```

> This operation is optimized and automatically converted from QueryRequest to GetItemRequest

Want to use the title index?! no problem...

```java
CatalogItem itemRetrieved=dsl.selectFrom(CATALOGITEM)
        .index(CatalogItem.TITLE)
        .where(CatalogItem.ID.eq(601)
        .and(CatalogItem.TITLE.eq("theTitle"))
        .fetchOne();
```

### Consistent Retrieve

**AWS**

```java
DynamoDBMapperConfig config=DynamoDBMapperConfig.builder()
        .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
        .build();
        CatalogItem updatedItem=mapper.load(CatalogItem.class,601,config);
```

**DynamoDSL**

```java
CatalogItem itemRetrieved=dsl.selectFrom(CATALOGITEM)
        .where(CatalogItem.ID.eq(601))
        .consistent()
        .fetchOne();
```

## Delete

**AWS**

```java
mapper.deleteOperation(new CatalogItem(601));
```

**DynamoDSL**

```java
boolean deleted=dsl.deleteFrom(CATALOGITEM)
        .withKey(CatalogItemKey.of(601))
        .execute();
```

or

```java
boolean deleted=dsl.deleteFrom(CATALOGITEM)
        .where(CatalogItem.ID.eq(601))
        .execute();
```

or

```java
record.deleteOperation();
```

or

```java
dsl.delete(CatalogItemKey.of(1,"123"));
```

or batched

```java
dsl.delete(dsl.deleteFrom(CATALOGITEM)
        .where(CATALOGITEM.ID.eq("1")),
        dsl.deleteFrom(ANOTHERITEM)
        .where(ANOTHERITEM.ID.eq(123)));
```

## Update

**AWS**

```java
CatalogItem itemRetrieved=mapper.load(CatalogItem.class,601);
        itemRetrieved.setISBN("622-2222222222");
        itemRetrieved.setBookAuthors(new HashSet<String>(Arrays.asList("Author1","Author3")));
        mapper.save(itemRetrieved);
```

**DynamoDSL**

```java
dsl.updateOperation(CATALOGITEM)
        .set(CatalogItem.ISBN,"622-2222222222")
        .set(CatalogItem.BOOKAUTHORS,new HashSet<String>(Arrays.asList("Author1","Author3")))
        .key(CatalogItemKey.of(601))
        .execute();
```

Conditional expression? piece of cake!

```java
dsl.updateOperation(CATALOGITEM)
        .set(CatalogItem.ISBN,"622-2222222222")
        .set(CatalogItem.BOOKAUTHORS,new HashSet<String>(Arrays.asList("Author1","Author3")))
        .key(CatalogItemKey.of(601))
        .when(CatalogItem.ISBN.notExists())
        .execute();
```

set the ISBN null if the passed value is null

```java
dsl.updateOperation(CATALOGITEM)
        .set(CatalogItem.ISBN,new NullableValue(someObject))
        .set(CatalogItem.BOOKAUTHORS,new HashSet<String>(Arrays.asList("Author1","Author3")))
        .key(CatalogItemKey.of(601))
        .when(CatalogItem.ISBN.notExists())
        .execute();
```

```java
dsl.updateOperation(CATALOGITEM)
        .setNull(CatalogItem.ISBN)
        .set(CatalogItem.BOOKAUTHORS,new HashSet<String>(Arrays.asList("Author1","Author3")))
        .key(CatalogItemKey.of(601))
        .when(CatalogItem.ISBN.notExists())
        .execute();
```

```java
record.updateOperation();
```

## Query

https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.QueryScanExample.html

```java
String partitionKey=forumName+"#"+threadSubject;

        long twoWeeksAgoMilli=(new Date()).getTime()-(15L*24L*60L*60L*1000L);
        Date twoWeeksAgo=new Date();
        twoWeeksAgo.setTime(twoWeeksAgoMilli);
        SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        String twoWeeksAgoStr=dateFormatter.format(twoWeeksAgo);
```

**AWS**

```java

Map<String, AttributeValue> eav=new HashMap<String, AttributeValue>();
        eav.putOperation(":val1",new AttributeValue().withS(partitionKey));
        eav.putOperation(":val2",new AttributeValue().withS(twoWeeksAgoStr.toString()));

        DynamoDBQueryExpression<Reply> queryExpression=new DynamoDBQueryExpression<Reply>()
        .withKeyConditionExpression("Id = :val1 and ReplyDateTime > :val2").withExpressionAttributeValues(eav);

        List<Reply> latestReplies=mapper.queryOperation(Reply.class,queryExpression);
```

> holy molly :O

**DynamoDSL**

```java
List<Reply> latestReplies=dsl.selectFrom(REPLY)
        .where(REPLY.ID.eq(forumName,threadSubject)
        .and(REPLY.REPLYDATETIME.greaterThan(twoWeeksAgoStr)))
        .fetch();
```

or

```java
List<ReplyDto> latestReplies=dsl.selectFrom(Reply)
        .where(REPLY.ID.eq(forumName,threadSubject)
        .and(REPLY.REPLYDATETIME.greaterThan(twoWeeksAgoStr)))
        .fetchInto(ReplyDto.class);
```

```java
List<ReplyDto> latestReplies=dsl.selectFrom(REPLY)
        .where(REPLY.ID.eq(forumName,threadSubject)
        .and(REPLY.REPLYDATETIME.greaterThan(twoWeeksAgoStr)))
        .mapping(this::toDto);
```

We don't have to explicit specify the Partition-Sort Key

```java
        List<Reply> latestReplies=dsl.selectFrom(REPLY)
        .where(REPLY.ID.eq(forumName,threadSubject)
        .and(REPLY.REPLYDATETIME.greaterThan(twoWeeksAgoStr)
        .and(REPLY.USERID.in(Set.of("123"))
        .or(REPLY.STATUS.eq(123)))
        .and(REPLY.SEEN.isTrue())
        ))
        .fetch();
```

The operation 'compiler' which is pretty fast, detects that there is only one value on the **in** condition and
transforms it to and equal condition.

## Scan

Scan and queryOperation are almost the same.

**AWS**

```java
 Map<String, AttributeValue> eav=new HashMap<String, AttributeValue>();
        eav.putOperation(":val1",new AttributeValue().withN(value));
        eav.putOperation(":val2",new AttributeValue().withS("Book"));

        DynamoDBScanExpression scanExpression=new DynamoDBScanExpression()
        .withFilterExpression("Price < :val1 and ProductCategory = :val2").withExpressionAttributeValues(eav);

        List<Book> scanResult=mapper.scanOperation(Book.class,scanExpression);
```

**DynamoDSL**

```java
List<Book> scanResult=dsl.scanOperation(PRODUCTS)
        .where(PRODUCT.PRICE.lessThan(value)
        .and(PRODUCT.PRODUCTCATEGORY.eq("Book"))
        .fetch();
```

## Features

### Intelligent optimizations

```java

boolean exists=dsl.fetchExists(dsl.selectFrom(PRODUCT)
        .where(PRODUCT.STOREID.eq(1)
        .and(PRODUCT.SKU.eq("sku"))

```

> This operation is optimized at runtime depending on their filters, scanOperation is not used at least is specified.

```
QueryRequest(TableName=product, Limit=1,
	FilterExpression=#sku = :sku,
	KeyConditionExpression=#storeId = :storeId, 
	ExpressionAttributeNames={#storeId=storeId, #sku=sku}, 
	ExpressionAttributeValues={:contentId=AttributeValue(N=1), :sku=AttributeValue(S=sku)})
```

### Lazy Fetching

```java
public class LazyRecord {

    String id;

    String name;

    @Lazy(value = "$id", table = "likes")
    List<String> likes;

}
```

### Late join

Information are fetched after the first operation

```java
dsl.selectFrom(PRODUCT)
        .lateJoin(MIXER.on(MIXER.UUID.startsWith(PRODUCT.UUID))
        .samePartition())
        .where(PRODUCT.CONTENTID.eq(123))
        .limit(20)
        .fetch();
```

### Eager Join

Join target is required

```java
public class JoinedRecord {

    String id;

    String name;

    @JoinTarget(value = "id", table = "mixer")
    List<String> likes;

}
```

```java
dsl.selectFrom(PRODUCT)
        .join(MIXER)
        .where(PRODUCT.CONTENTID.eq(123))
        .limit(20)
        .fetch();
```

```java
dsl.selectFrom(PRODUCT)
        .join(dsl.selectFrom(MIXER)
        .where(MIXER.CONTENTID.eq(PRODUCT.CONTENTID)))
        .where(PRODUCT.CONTENTID.eq(123))
        .limit(20)
        .fetch();
```