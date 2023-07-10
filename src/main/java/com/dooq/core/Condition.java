package com.dooq.core;

import com.dooq.api.Column;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Condition {

    private final String command;

    public Condition(String command) {
        this.command = command;
    }

    @Contract("_ -> new")
    public static @NotNull Condition notExists(@NotNull Column<?, ?> column) {
        return attribute_not_exists(column.name());
    }

    @Contract("_ -> new")
    public static @NotNull Condition exists(@NotNull Column<?, ?> column) {
        return attribute_exists(column.name());
    }

    public static @NotNull Condition attribute_exists(@NotNull Object obj) {
        return new Condition("attribute_exists(" + obj + ")");
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Condition attribute_not_exists(@NotNull Object obj) {
        return new Condition("attribute_not_exists(" + obj + ")");
    }

    @Contract("_, _ -> new")
    public static @NotNull Condition begins_with(@NotNull Column<?, ?> column, String content) {
        return new Condition("begins_with(" + column.name() + "," + content + ")");
    }

    @Contract("_, _ -> new")
    public static @NotNull Condition contains(@NotNull Column<?, ?> column, String content) {
        return new Condition("contains(" + column.name() + "," + content + ")");
    }

    public String getCommand() {
        return command;
    }
}
