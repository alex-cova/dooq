package org.dooq.api;


import org.dooq.Key;
import org.dooq.core.schema.Index;
import org.dooq.join.MergeExpression;
import org.dooq.parser.ObjectParser;
import org.dooq.parser.ParserCompiler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Table<R extends AbstractRecord<R>, K extends Key> {

    public abstract List<Column<R, K>> getColumns();

    public abstract String getTableName();

    public abstract Class<R> getRecordType();

    public abstract Column<R, K> getPartitionColumn();

    public abstract Column<R, K> getSortColumn();

    private volatile ObjectParser<R> recordParser;

    @ApiStatus.Experimental
    public MergeExpression<?> on(MergeExpression<?> expression) {
        return expression;
    }

    public Map<String, Index> getIndices() {
        return Collections.emptyMap();
    }

    public @Nullable Index getIndex(String name) {
        return getIndices().get(name);
    }

    public ObjectParser<R> getRecordParser() {

        if (recordParser == null) {
            synchronized (this) {
                if (recordParser == null) {
                    recordParser = ParserCompiler.getConverter(getRecordType());
                }
            }
        }

        return recordParser;
    }

    private List<Column<R, K>> columns;

    @SuppressWarnings("unchecked")
    public synchronized List<Column<R, K>> columns() {

        if (this.columns != null) return this.columns;

        return this.columns = Arrays.stream(getClass().getDeclaredFields())
                .filter(a -> a.getType() == Field.class)
                .map(a -> {
                    try {
                        return (Column<R, K>) a.get(this);
                    } catch (IllegalAccessException e) {
                        Logger.getLogger(Table.class.getName())
                                .log(Level.SEVERE, null, e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
