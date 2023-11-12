package org.dooq.util;

import org.dooq.api.AbstractRecord;
import org.dooq.Key;
import org.dooq.engine.ExpressionRenderer;

import java.util.List;
import java.util.Objects;

public final class Split<R extends AbstractRecord<R>, K extends Key> {
    private final List<ExpressionRenderer<R, K>> keys;
    private final List<ExpressionRenderer<R, K>> nonKeys;

    public Split(List<ExpressionRenderer<R, K>> keys,
                 List<ExpressionRenderer<R, K>> nonKeys) {
        this.keys = keys;
        this.nonKeys = nonKeys;
    }

    public List<ExpressionRenderer<R, K>> keys() {
        return keys;
    }

    public List<ExpressionRenderer<R, K>> nonKeys() {
        return nonKeys;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Split) obj;
        return Objects.equals(this.keys, that.keys) &&
                Objects.equals(this.nonKeys, that.nonKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys, nonKeys);
    }

    @Override
    public String toString() {
        return "Split[" +
                "keys=" + keys + ", " +
                "nonKeys=" + nonKeys + ']';
    }

}
