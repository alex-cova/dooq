package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.api.PartitionKey;
import org.dooq.api.SortKey;
import org.dooq.api.DynamoDBTable;

import java.math.BigDecimal;
import java.util.List;

@DynamoDBTable("Mixer")
public class MixerRecord extends AbstractRecord<MixerRecord> {

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

    public MixerRecord() {
    }

    @Override
    public Key getKey() {
        return MixerKey.of(parentUuid, uuid);
    }

    public MixerRecord setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public MixerRecord setPrint(Boolean print) {
        this.print = print;
        return this;
    }

    public Boolean getPrint() {
        return print;
    }

    public MixerRecord setContentId(long contentId) {
        this.contentId = contentId;
        return this;
    }

    public long getContentId() {
        return contentId;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public MixerRecord setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public MixerRecord setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public MixerRecord setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public MixerRecord setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    public MixerRecord setOrder(Integer order) {
        this.order = order;
        return this;
    }

    public Boolean getExtra() {
        return extra;
    }

    public MixerRecord setExtra(Boolean extra) {
        this.extra = extra;
        return this;
    }

    public String getTagName() {
        return tagName;
    }

    public MixerRecord setTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public BigDecimal getExtraCost() {
        return extraCost;
    }

    public MixerRecord setExtraCost(BigDecimal extraCost) {
        this.extraCost = extraCost;
        return this;
    }

    public Integer getMaximum() {
        return maximum;
    }

    public MixerRecord setMaximum(Integer maximum) {
        this.maximum = maximum;
        return this;
    }

    public Integer getMinimum() {
        return minimum;
    }

    public MixerRecord setMinimum(Integer minimum) {
        this.minimum = minimum;
        return this;
    }

    public BigDecimal getPortion() {
        return portion;
    }

    public MixerRecord setPortion(BigDecimal portion) {
        this.portion = portion;
        return this;
    }

    public String getProductUuid() {
        return productUuid;
    }

    public MixerRecord setProductUuid(String productUuid) {
        this.productUuid = productUuid;
        return this;
    }

    public List<MixerContentRecord> getContent() {
        return content;
    }

    public MixerRecord setContent(List<MixerContentRecord> content) {
        this.content = content;
        return this;
    }
}
