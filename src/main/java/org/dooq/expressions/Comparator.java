package org.dooq.expressions;

public enum Comparator {

    GREATER(">"),
    GREATER_OR_EQUAL("=>"),
    LESS("<"),
    LESS_OR_EQUAL("<="),
    NEITHER("<>"),
    EQUALS("=");

    final String operator;

    Comparator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
