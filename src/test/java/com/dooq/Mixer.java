package com.dooq;


import com.dooq.api.*;
import com.dooq.core.schema.Index;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Mixer implements Table<MixerRecord, MixerKey> {

    public final Field<String, MixerRecord, MixerKey> PARENTUUID = FieldBuilder.partition("parentUuid", String.class, this);
    public final Field<String, MixerRecord, MixerKey> UUID = FieldBuilder.sort("uuid", String.class, this);
    public final Field<String, MixerRecord, MixerKey> GROUPUUID = FieldBuilder.of("groupUuid", String.class, this);
    public final Field<String, MixerRecord, MixerKey> QUANTITY = FieldBuilder.of("quantity", String.class, this);
    public final Field<String, MixerRecord, MixerKey> ORDER = FieldBuilder.of("order", String.class, this);
    public final Field<String, MixerRecord, MixerKey> CONTENTID = FieldBuilder.of("contentId", String.class, this);
    public final Field<String, MixerRecord, MixerKey> EXTRA = FieldBuilder.of("extra", String.class, this);
    public final Field<String, MixerRecord, MixerKey> TAGNAME = FieldBuilder.of("tagName", String.class, this);
    public final Field<String, MixerRecord, MixerKey> EXTRACOST = FieldBuilder.of("extraCost", String.class, this);
    public final Field<String, MixerRecord, MixerKey> MAXIMUM = FieldBuilder.of("maximum", String.class, this);
    public final Field<String, MixerRecord, MixerKey> MINIMUM = FieldBuilder.of("minimum", String.class, this);
    public final Field<String, MixerRecord, MixerKey> PORTION = FieldBuilder.of("portion", String.class, this);
    public final Field<String, MixerRecord, MixerKey> PRODUCTUUID = FieldBuilder.of("productUuid", String.class, this);
    public final Field<String, MixerRecord, MixerKey> CONTENT = FieldBuilder.of("content", String.class, this);
    public final Field<String, MixerRecord, MixerKey> PRINT = FieldBuilder.of("print", String.class, this);
    public final Field<String, MixerRecord, MixerKey> CATEGORY = FieldBuilder.of("category", String.class, this);
    private final List<Column<MixerRecord, MixerKey>> COLUMNS;
    private final Map<String, Index> INDICES;

    public Mixer() {

        COLUMNS = columns();

        INDICES = Index.builder(this)
                .localOnlyKeys(PRODUCTUUID)
                .localOnlyKeys(TAGNAME)
                .globalOnlyKeys("group", CONTENTID, GROUPUUID)
                .globalInclude(CONTENT, CONTENTID, UUID, Set.of(CONTENT, PRODUCTUUID))
                .build();
    }

    @Override
    public Map<String, Index> getIndices() {
        return INDICES;
    }

    @Override
    public List<Column<MixerRecord, MixerKey>> getColumns() {
        return COLUMNS;
    }

    @Override
    public String getTableName() {
        return "mixer";
    }

    @Override
    public Class<MixerRecord> getRecordType() {
        return MixerRecord.class;
    }

    @Override
    public Column<MixerRecord, MixerKey> getPartitionColumn() {
        return PARENTUUID;
    }

    @Override
    public Column<MixerRecord, MixerKey> getSortColumn() {
        return UUID;
    }


}
