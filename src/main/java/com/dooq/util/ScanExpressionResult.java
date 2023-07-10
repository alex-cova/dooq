package com.dooq.util;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ScanExpressionResult<R extends AbstractRecord<R>, K extends Key> {
    private final String expression;
    private final Map<String, Object> values;
    private final Map<String, String> attributeNames;
    private final List<Column<R, K>> columns;

    public ScanExpressionResult(String expression,
                                Map<String, Object> values,
                                Map<String, String> attributeNames,
                                List<Column<R, K>> columns) {
        this.expression = expression;
        this.values = values;
        this.attributeNames = attributeNames;
        this.columns = columns;
    }

    public String expression() {
        return expression;
    }

    public Map<String, Object> values() {
        return values;
    }

    public Map<String, String> attributeNames() {
        return attributeNames;
    }

    public List<Column<R, K>> columns() {
        return columns;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ScanExpressionResult) obj;
        return Objects.equals(this.expression, that.expression) &&
                Objects.equals(this.values, that.values) &&
                Objects.equals(this.attributeNames, that.attributeNames) &&
                Objects.equals(this.columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, values, attributeNames, columns);
    }

    @Override
    public String toString() {
        return "ScanExpressionResult[" +
                "expression=" + expression + ", " +
                "values=" + values + ", " +
                "attributeNames=" + attributeNames + ", " +
                "columns=" + columns + ']';
    }


}
