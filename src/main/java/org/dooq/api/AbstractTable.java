package org.dooq.api;


import org.dooq.util.AbstractColumn;

import java.util.Collections;
import java.util.List;

public class AbstractTable extends Table<AbstractRecord, AbstractKey> {

    private final String tableName;
    private AbstractColumn<AbstractRecord, AbstractKey> partitionColumn;
    private AbstractColumn<AbstractRecord, AbstractKey> sortColumn;

    public AbstractTable(String tableName) {
        this.tableName = tableName;
    }

    public AbstractTable setPartitionColumn(String name) {
        this.partitionColumn = new AbstractColumn<>(this, name);
        return this;
    }

    public AbstractTable setSortColumn(String name) {
        this.sortColumn = new AbstractColumn<>(this, name);
        return this;
    }

    @Override
    public List<Column<AbstractRecord, AbstractKey>> getColumns() {
        return Collections.emptyList();
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Class<AbstractRecord> getRecordType() {
        return AbstractRecord.class;
    }

    @Override
    public Column<AbstractRecord, AbstractKey> getPartitionColumn() {
        return partitionColumn;
    }

    @Override
    public Column<AbstractRecord, AbstractKey> getSortColumn() {
        return sortColumn;
    }
}
