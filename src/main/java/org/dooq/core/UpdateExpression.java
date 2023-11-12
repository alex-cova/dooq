package org.dooq.core;

import org.dooq.api.Column;

import java.util.Objects;

public final class UpdateExpression implements Expression {

    private final Column<?, ?> column;
    private final String operation;
    private final String param;

    public UpdateExpression(Column<?,?> column, String operation, String param) {
        this.column = column;
        this.operation = operation;
        this.param = param;
    }

    @Override
    public String render() {
        return column.escapedName() + " " + operation + " " + param;
    }

    public Column<?, ?> column() {
        return column;
    }

    public String operation() {
        return operation;
    }

    public String param() {
        return param;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UpdateExpression) obj;
        return Objects.equals(this.column, that.column) &&
                Objects.equals(this.operation, that.operation) &&
                Objects.equals(this.param, that.param);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, operation, param);
    }

    @Override
    public String toString() {
        return "UpdateExpression[" +
                "column=" + column + ", " +
                "operation=" + operation + ", " +
                "param=" + param + ']';
    }

}
