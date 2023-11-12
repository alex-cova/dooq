package org.dooq.core;

import org.dooq.api.Column;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class DynamoSemantics {

    public static @NotNull String param(Column<?, ?> column) {
        return param(column.name());
    }

    public static @NotNull String param(@NotNull String value) {
        return ":" + value;
    }

    public static @NotNull String escaped(Column<?, ?> column) {
        return escaped(column.name());
    }

    @Contract(pure = true)
    public static @NotNull String escaped(String value) {
        return "#" + value;
    }
}
