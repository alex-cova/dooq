package org.dooq.scheme;

import lombok.Data;
import org.dooq.Key;
import org.dooq.api.DynamoDBTable;
import org.dooq.api.DynamoRecord;
import org.dooq.api.PartitionKey;
import org.dooq.api.SortKey;

import java.math.BigDecimal;
import java.util.List;

@DynamoDBTable("Mixer")
@Data
public class MixerRecord extends DynamoRecord<MixerRecord> {

    @PartitionKey
    private String parentUuid;
    @SortKey
    private String uuid;
    private String groupUuid;
    private Integer quantity;
    private Integer order;
    private Boolean extra;
    private String tagName;
    private BigDecimal extraCost;
    private Integer maximum;
    private Integer minimum;
    private BigDecimal portion;
    private String productUuid;
    private List<MixerContentRecord> content;
    private long contentId;
    private Boolean print;
    private String category;


    @Override
    public Key getKey() {
        return MixerKey.of(parentUuid, uuid);
    }


}
