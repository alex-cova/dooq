package com.dooq.core;

import com.dooq.api.Column;

public class ComplexExpression implements Expression {

    private final Column<?, ?> column;
    private final String expression;

    public ComplexExpression(Column<?, ?> column, String expression) {
        this.column = column;
        this.expression = expression;
    }

    public Column<?, ?> column() {
        return column;
    }

    public String expression() {
        return expression;
    }

    @Override
    public String render() {
        return expression;
    }
}
