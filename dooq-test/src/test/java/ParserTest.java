import org.dooq.api.DynamoConverter;
import org.dooq.test.ProductsRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    void name() {
        var parser = DynamoConverter.getConverter(ProductsRecord.class);

        var record = new ProductsRecord()
                .setPartition("partition")
                .setSort("sort")
                .setDescription("description")
                .setCategoryUuid("categoryId");

        var result = parser.write(record);

        for (String s : result.keySet()) {
            System.out.println(s);
        }

        var newRecord = parser.read(result);

        Assertions.assertEquals(record.getPartition(), newRecord.getPartition());
        Assertions.assertEquals(record.getSort(), newRecord.getSort());
        Assertions.assertEquals(record.getDescription(), newRecord.getDescription());
        Assertions.assertEquals(record.getCategoryUuid(), newRecord.getCategoryUuid());

    }
}
