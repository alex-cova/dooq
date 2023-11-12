package org.dooq;

import org.dooq.api.AbstractRecord;
import org.dooq.api.PartitionKey;
import org.dooq.api.SortKey;
import org.dooq.api.DynamoDBTable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@DynamoDBTable("Product")
public class ProductRecord extends AbstractRecord<ProductRecord> {

    @SortKey
    private String uuid;
    @PartitionKey
    private Long contentId;
    private String sku;
    private Set<String> skus;
    private String description;
    private String imageUrl;
    private String details;
    private String purchaseUnitId;
    private String salesUnitId;
    private BigDecimal factor;
    private Boolean bulk;
    private Boolean autoWeigh;
    private Boolean favorite;
    private String claveSat;
    private String departmentId;
    private String categoryId;
    private BigDecimal waste;
    private Integer type;
    private BigDecimal weight;
    private String customsName;
    private LocalDate customsDocDate;
    private String customsDocNumber;
    private String propertyAccount;
    private BigDecimal minProfitPercentage;
    private BigDecimal avgPurchasePrice;
    private LocalDateTime created;
    private String userCreatorId;
    private LocalDateTime modified;
    private String userModifierId;
    private Integer subscriptionAmount;
    private Integer subscriptionUnit;
    private Boolean saleable;
    private Map<String, BigDecimal> prices;

    private MixerContentRecord mixers;

    public ProductRecord() {
    }

    @Override
    public Key getKey() {
        return ProductKey.of(contentId, uuid);
    }

    public String getUuid() {
        return uuid;
    }

    public ProductRecord setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Long getContentId() {
        return contentId;
    }

    public ProductRecord setContentId(Long contentId) {
        this.contentId = contentId;
        return this;
    }

    public String getSku() {
        return sku;
    }

    public ProductRecord setSku(String sku) {
        this.sku = sku;
        return this;
    }

    public Set<String> getSkus() {
        return skus;
    }

    public ProductRecord setSkus(Set<String> skus) {
        this.skus = skus;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ProductRecord setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ProductRecord setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public String getDetails() {
        return details;
    }

    public ProductRecord setDetails(String details) {
        this.details = details;
        return this;
    }

    public String getPurchaseUnitId() {
        return purchaseUnitId;
    }

    public ProductRecord setPurchaseUnitId(String purchaseUnitId) {
        this.purchaseUnitId = purchaseUnitId;
        return this;
    }

    public String getSalesUnitId() {
        return salesUnitId;
    }

    public ProductRecord setSalesUnitId(String salesUnitId) {
        this.salesUnitId = salesUnitId;
        return this;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public ProductRecord setFactor(BigDecimal factor) {
        this.factor = factor;
        return this;
    }

    public Boolean getBulk() {
        return bulk;
    }

    public ProductRecord setBulk(Boolean bulk) {
        this.bulk = bulk;
        return this;
    }

    public Boolean getAutoWeigh() {
        return autoWeigh;
    }

    public ProductRecord setAutoWeigh(Boolean autoWeigh) {
        this.autoWeigh = autoWeigh;
        return this;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public ProductRecord setFavorite(Boolean favorite) {
        this.favorite = favorite;
        return this;
    }

    public String getClaveSat() {
        return claveSat;
    }

    public ProductRecord setClaveSat(String claveSat) {
        this.claveSat = claveSat;
        return this;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public ProductRecord setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public ProductRecord setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public BigDecimal getWaste() {
        return waste;
    }

    public ProductRecord setWaste(BigDecimal waste) {
        this.waste = waste;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public ProductRecord setType(Integer type) {
        this.type = type;
        return this;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public ProductRecord setWeight(BigDecimal weight) {
        this.weight = weight;
        return this;
    }

    public String getCustomsName() {
        return customsName;
    }

    public ProductRecord setCustomsName(String customsName) {
        this.customsName = customsName;
        return this;
    }

    public LocalDate getCustomsDocDate() {
        return customsDocDate;
    }

    public ProductRecord setCustomsDocDate(LocalDate customsDocDate) {
        this.customsDocDate = customsDocDate;
        return this;
    }

    public String getCustomsDocNumber() {
        return customsDocNumber;
    }

    public ProductRecord setCustomsDocNumber(String customsDocNumber) {
        this.customsDocNumber = customsDocNumber;
        return this;
    }

    public String getPropertyAccount() {
        return propertyAccount;
    }

    public ProductRecord setPropertyAccount(String propertyAccount) {
        this.propertyAccount = propertyAccount;
        return this;
    }

    public BigDecimal getMinProfitPercentage() {
        return minProfitPercentage;
    }

    public ProductRecord setMinProfitPercentage(BigDecimal minProfitPercentage) {
        this.minProfitPercentage = minProfitPercentage;
        return this;
    }

    public BigDecimal getAvgPurchasePrice() {
        return avgPurchasePrice;
    }

    public ProductRecord setAvgPurchasePrice(BigDecimal avgPurchasePrice) {
        this.avgPurchasePrice = avgPurchasePrice;
        return this;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public ProductRecord setCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public String getUserCreatorId() {
        return userCreatorId;
    }

    public ProductRecord setUserCreatorId(String userCreatorId) {
        this.userCreatorId = userCreatorId;
        return this;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public ProductRecord setModified(LocalDateTime modified) {
        this.modified = modified;
        return this;
    }

    public String getUserModifierId() {
        return userModifierId;
    }

    public ProductRecord setUserModifierId(String userModifierId) {
        this.userModifierId = userModifierId;
        return this;
    }

    public Integer getSubscriptionAmount() {
        return subscriptionAmount;
    }

    public ProductRecord setSubscriptionAmount(Integer subscriptionAmount) {
        this.subscriptionAmount = subscriptionAmount;
        return this;
    }

    public Integer getSubscriptionUnit() {
        return subscriptionUnit;
    }

    public ProductRecord setSubscriptionUnit(Integer subscriptionUnit) {
        this.subscriptionUnit = subscriptionUnit;
        return this;
    }

    public Boolean getSaleable() {
        return saleable;
    }

    public ProductRecord setSaleable(Boolean saleable) {
        this.saleable = saleable;
        return this;
    }

    public Map<String, BigDecimal> getPrices() {
        return prices;
    }

    public ProductRecord setPrices(Map<String, BigDecimal> prices) {
        this.prices = prices;
        return this;
    }

    @Override
    public String toString() {
        return "ProductRecord{" +
                "uuid='" + uuid + '\'' +
                ", contentId=" + contentId +
                ", sku='" + sku + '\'' +
                ", skus='" + skus + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", details='" + details + '\'' +
                ", purchaseUnitId='" + purchaseUnitId + '\'' +
                ", salesUnitId='" + salesUnitId + '\'' +
                ", factor=" + factor +
                ", bulk=" + bulk +
                ", autoWeigh=" + autoWeigh +
                ", favorite=" + favorite +
                ", claveSat='" + claveSat + '\'' +
                ", departmentId='" + departmentId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", waste=" + waste +
                ", type=" + type +
                ", saleable=" + saleable +
                ", weight=" + weight +
                ", customsName='" + customsName + '\'' +
                ", customsDocDate=" + customsDocDate +
                ", customsDocNumber='" + customsDocNumber + '\'' +
                ", propertyAccount='" + propertyAccount + '\'' +
                ", minProfitPercentage=" + minProfitPercentage +
                ", avgPurchasePrice=" + avgPurchasePrice +
                ", created=" + created +
                ", userCreatorId='" + userCreatorId + '\'' +
                ", modified=" + modified +
                ", userModifierId='" + userModifierId + '\'' +
                '}';
    }
}
