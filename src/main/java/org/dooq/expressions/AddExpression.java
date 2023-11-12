package org.dooq.expressions;

import org.dooq.api.Column;
import org.dooq.core.Expression;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record AddExpression(Column<?, ?> column, BigDecimal value) implements Expression {

    @Override
    public @NotNull String render() {
        return column.escapedName() + " " + column.param();
    }
}

