package org.dooq.util;

import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.api.ColumnType;
import org.dooq.api.Table;

import java.util.Objects;

public record AbstractColumn<R extends AbstractRecord<R>, K
        extends Key>(Table<R, K> table, String name) implements Column<R, K> {

    @Override
    public ColumnType columnType() {
        return ColumnType.NORMAL;
    }

    @Override
    public boolean index() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AbstractColumn) obj;
        return Objects.equals(this.table, that.table) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public String toString() {
        return "AbstractColumn[" +
                "table=" + table + ", " +
                "name=" + name + ']';
    }


}
