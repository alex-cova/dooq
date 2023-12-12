package org.dooq.scheme;


import java.math.BigDecimal;

public class MixerContentRecord {

    private String uuid;
    private BigDecimal extraCost;
    private String tagName;
    private Integer quantity;
    private boolean print;
    private BigDecimal portion;

    public MixerContentRecord() {

    }

    public MixerContentRecord(String uuid, BigDecimal extraCost, String tagName, Integer quantity, boolean print, BigDecimal portion) {
        this.uuid = uuid;
        this.extraCost = extraCost;
        this.tagName = tagName;
        this.quantity = quantity;
        this.print = print;
        this.portion = portion;
    }

    public String getUuid() {
        return uuid;
    }

    public MixerContentRecord setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public BigDecimal getExtraCost() {
        return extraCost;
    }

    public MixerContentRecord setExtraCost(BigDecimal extraCost) {
        this.extraCost = extraCost;
        return this;
    }

    public String getTagName() {
        return tagName;
    }

    public MixerContentRecord setTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public MixerContentRecord setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public boolean isPrint() {
        return print;
    }

    public MixerContentRecord setPrint(boolean print) {
        this.print = print;
        return this;
    }

    public BigDecimal getPortion() {
        return portion;
    }

    public MixerContentRecord setPortion(BigDecimal portion) {
        this.portion = portion;
        return this;
    }

    @Override
    public String toString() {
        return "ComboContentRecord{" +
                "uuid='" + uuid + '\'' +
                ", extraCost=" + extraCost +
                ", tagName='" + tagName + '\'' +
                ", quantity=" + quantity +
                ", print=" + print +
                ", portion=" + portion +
                '}';
    }
}
