package com.dooq.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EscapedProjection(List<String> result, Map<String, String> attributeNames) {

    public boolean hasAttributeNames() {
        return !attributeNames.isEmpty();
    }

    public boolean hasResults() {
        return !result.isEmpty();
    }

    @Contract(" -> new")
    public @NotNull String join() {
        return String.join(",", result);
    }

    public @NotNull Map<String, String> merge(Map<String, String> map) {
        var result = new HashMap<String, String>();

        result.putAll(map);
        result.putAll(attributeNames);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EscapedProjection) obj;
        return Objects.equals(this.result, that.result) &&
                Objects.equals(this.attributeNames, that.attributeNames);
    }

    @Override
    public String toString() {
        return "EscapedProjection[" +
                "result=" + result + ", " +
                "attributeNames=" + attributeNames + ']';
    }

}
